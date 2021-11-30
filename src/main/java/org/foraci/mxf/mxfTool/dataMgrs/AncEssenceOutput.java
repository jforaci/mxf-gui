package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.anc.util.io.MultiplexingInputStream;
import org.foraci.mxf.mxfReader.EssenceContainerOutputController;
import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.anc.Mxf436AncTrackReader;
import org.foraci.mxf.mxfReader.util.MxfReaderUtility;
import org.foraci.mxf.mxfTool.cc.CaptionSinkRunner;
import org.foraci.mxf.mxfTool.dolbye.DolbyEConsumer;
import org.foraci.mxf.mxfTool.gui.MxfView;
import org.foraci.mxf.mxfTool.mxf.MxfAncReader;
import org.foraci.anc.util.timecode.Timecode;
import org.foraci.anc.util.timecode.TimecodeBase;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Essence output controller
 */
public class AncEssenceOutput implements EssenceContainerOutputController
{
    protected Map<EssenceTrack, PipedOutputStream> fout = new HashMap<EssenceTrack, PipedOutputStream>();
    private final MxfAncReader ancReader;
    private final List<EssenceTrack> tracks;
    private final Map<String, EssenceTrack> trackMap;
    private MxfView view;
    private CaptionSinkRunner sink;
    private boolean done;
    private int sysItemCount, cpCount;
    private long firstTrackNumber = 0;

    public AncEssenceOutput(MxfAncReader ancReader, List<EssenceTrack> tracks, MxfView view) {
        this.ancReader = ancReader;
        this.tracks = tracks;
        this.trackMap = new HashMap<String, EssenceTrack>();
        for (EssenceTrack track : tracks) {
            String key = "b" + track.getBodySid() + "t" + track.getTrackNumber();
            trackMap.put(key, track);
        }
        this.view = view;
        sink = new CaptionSinkRunner(view);
        this.done = false;
        this.sysItemCount = this.cpCount = 0;
    }

    public boolean needEssence() {
        return !done;
    }

    public OutputStream getOutputForBodySidAndTrack(long bodySid, UL ul) throws IOException {
        long trackNumber = ul.getTrackNumber();
        EssenceTrack track = getTrackIgnored(bodySid, ul);
        if (track == null) { // ignored track
            return null;
        }
        if (firstTrackNumber == 0) {
            firstTrackNumber = trackNumber;
        } else if (firstTrackNumber == trackNumber) {
            cpCount++;
            checkForDone();
        }
        if (ul.isDataEssence() && (trackNumber == SMPTE436_VBI_TRACK || trackNumber == SMPTE436_ANC_TRACK)) {
            if (!ancReader.isDecodeCea708() && !ancReader.isDecodeAncillaryPackets()) {
                return null;
            }
            return getCaptionsSinkForSmpte436(track, (trackNumber == SMPTE436_ANC_TRACK));
        } else if (ul.isSoundEssence() && ancReader.isDecodeDolbyE()) {
            return getDolbyESink(track);
        }
        return null;
    }

    private EssenceTrack getTrackIgnored(long bodySid, UL ul) {
        String key = "b" + String.valueOf(bodySid) + "t" + ul.getTrackNumber();
        return trackMap.get(key);
    }

    private Map<EssenceTrack, Thread> ancReaderMap = new HashMap<EssenceTrack, Thread>();

    private OutputStream getCaptionsSinkForSmpte436(EssenceTrack track, boolean isAnc)
            throws IOException {
        PipedOutputStream os = fout.get(track);
        if (os == null) {
            os = new PipedOutputStream();
            PipedInputStream in = new PipedInputStream(os);
            Mxf436AncTrackReader.Type type = (isAnc) ? Mxf436AncTrackReader.Type.ANC : Mxf436AncTrackReader.Type.VBI;
            MxfReaderUtility mxfReaderUtility = new MxfReaderUtility(view.getGroups());
            Timecode timecode = Timecode.fromTimecode(
                    TimecodeBase.NTSC,
                    mxfReaderUtility.getStartTimecode().getLabel());
            Mxf436AncTrackReader ancTrackReader = new Mxf436AncTrackReader(in, type, timecode);
            ancTrackReader.setDebug(false);
            if (ancReader.isDecodeCea708()) {
                ancTrackReader.setDecode708(true);
                ancTrackReader.register708CaptionSink(sink);
            }
            if (ancReader.isDecodeAncillaryPackets()) {
                ancTrackReader.addAncPacketListener(view.getAncPacketListener());
            }
            Thread ancThread = new Thread(ancTrackReader, "ANC-READER");
            ancThread.start();
            fout.put(track, os);
            ancReaderMap.put(track, ancThread);
        }
        return os;
    }

    private Map<EssenceTrack, DolbyEConsumer> eReaderMap = new HashMap<EssenceTrack, DolbyEConsumer>();

    private OutputStream getDolbyESink(EssenceTrack track)
            throws IOException {
        PipedOutputStream os = fout.get(track);
        if (os == null) {
            os = new PipedOutputStream();
            BufferedInputStream in = new BufferedInputStream(new PipedInputStream(os));
            MultiplexingInputStream muxInputStream = new MultiplexingInputStream(new DataInputStream(in), null, 3);
            DolbyEConsumer consumer = new DolbyEConsumer(
                    muxInputStream, view, 3, false, true, track);
            new Thread(consumer, "DOLBYE-READER").start();
            fout.put(track, os);
            eReaderMap.put(track, consumer);
        } else if (eReaderMap.get(track).isDone()) {
            return null;
        }
        return os;
    }

    public OutputStream getOutputForSystemElement(long bodySid, UL ul) throws IOException {
        if (ul.isFirstGcSystemElementInCp()) {
            sysItemCount++;
        }
        if (sysItemCount % 10 == 0) {
            checkForDone();
        }
        return null;
    }

    private void checkForDone() {
        if (ancReader.isDecodeCea708()) {
            for (Iterator<Thread> i = ancReaderMap.values().iterator(); i.hasNext();) {
                Thread consumer = i.next();
                if (consumer.isAlive()) {
                    return;
                }
            }
        }
        if (ancReader.isDecodeDolbyE()) {
            for (Iterator<DolbyEConsumer> i = eReaderMap.values().iterator(); i.hasNext();) {
                DolbyEConsumer consumer = i.next();
                if (!consumer.isDone()) {
                    return;
                }
            }
        }
        if (ancReader.isDecodeAncillaryPackets() && !view.getAncPacketListener().done()) {
            return;
        }
        done = true; // tell MxfReader that we're done with essence data
    }

    public void close() throws IOException {
        for (Iterator i = fout.values().iterator(); i.hasNext();) {
            OutputStream fos = (OutputStream) i.next();
            fos.flush();
            fos.close();
        }
        ancReaderMap.clear();
        for (Iterator<DolbyEConsumer> i = eReaderMap.values().iterator(); i.hasNext();) {
            DolbyEConsumer consumer = i.next();
            synchronized (consumer) {
                consumer.kill();
                consumer.notifyAll(); // let consumer die
            }
        }
        eReaderMap.clear();
    }
}
