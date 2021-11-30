package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.mxf.mxfReader.EssenceFileOutputController;
import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.anc.Mxf436AncTrackReader;
import org.foraci.mxf.mxfReader.util.MxfReaderUtility;
import org.foraci.mxf.mxfTool.gui.MxfView;
import org.foraci.anc.util.timecode.Timecode;
import org.foraci.anc.util.timecode.TimecodeBase;

import java.io.*;
import java.util.List;

/**
 * Essence output controller
 */
public class EssenceOutput extends EssenceFileOutputController {
    private final MxfView view;
    private final List<EssenceTrack> tracks;
    private final boolean createCaptionOnlyFile;
    private final boolean zeroBasedCaptionFile;

    public EssenceOutput(File baseName, MxfView view, List<EssenceTrack> tracks, boolean createCaptionOnlyFile,
                         boolean zeroBasedCaptionFile) {
        super(baseName);
        this.view = view;
        this.tracks = tracks;
        this.createCaptionOnlyFile = createCaptionOnlyFile;
        this.zeroBasedCaptionFile = zeroBasedCaptionFile;
    }

    @Override
    public OutputStream getOutputForBodySidAndTrack(long bodySid, UL ul) throws IOException {
        long trackNumber = ul.getTrackNumber();
        boolean found = false;
        for (EssenceTrack t : tracks) {
            if (t.getBodySid() == bodySid && t.getTrackNumber() == trackNumber && baseName.equals(t.getFile())) {
                found = true;
                break;
            }
        }
        if (!found) {
            return null;
        }
        if (createCaptionOnlyFile && ul.isDataEssence() && (trackNumber == SMPTE436_VBI_TRACK || trackNumber == SMPTE436_ANC_TRACK)) {
            return getOutputForSmpte436Scc(bodySid, ul);
        }
        return super.getOutputForBodySidAndTrack(bodySid, ul);
    }

    private OutputStream getOutputForSmpte436Scc(long bodySid, UL ul)
            throws IOException {
        long trackNumber = ul.getTrackNumber();
        String key = "b" + String.valueOf(bodySid) + "t" + String.valueOf(trackNumber);
        PipedOutputStream os = (PipedOutputStream) fout.get(key);
        if (os == null) {
            String suffix = "-" + key + ".ccd";
            os = new PipedOutputStream();
            PipedInputStream in = new PipedInputStream(os);
            File cc608out = new File(baseName.getParentFile(), baseNameFilename() + suffix);
            Mxf436AncTrackReader.Type type = (trackNumber == SMPTE436_ANC_TRACK) ? Mxf436AncTrackReader.Type.ANC : Mxf436AncTrackReader.Type.VBI;
            MxfReaderUtility mxfReaderUtility = new MxfReaderUtility(view.getGroups());
            Timecode timecode = Timecode.fromTimecode(
                    TimecodeBase.NTSC,
                    mxfReaderUtility.getStartTimecode().getLabel());
            Mxf436AncTrackReader ancReader = new Mxf436AncTrackReader(in, type, timecode);
//            ancReader.setDebug(true);
            ancReader.setCcd608OutputStream(new BufferedOutputStream(new FileOutputStream(cc608out)));
            new Thread(ancReader, "ANC-READER").start();
            fout.put(key, os);
        }
        return os;
    }
}
