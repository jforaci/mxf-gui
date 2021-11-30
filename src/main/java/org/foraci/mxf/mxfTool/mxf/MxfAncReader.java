package org.foraci.mxf.mxfTool.mxf;

import org.foraci.mxf.mxfReader.EssenceContainerOutputController;
import org.foraci.mxf.mxfReader.MxfReader;
import org.foraci.mxf.mxfTool.dataMgrs.AncEssenceOutput;
import org.foraci.mxf.mxfTool.dataMgrs.EssenceTrack;
import org.foraci.mxf.mxfTool.gui.MxfView;

import java.io.File;
import java.util.List;

/**
 * Reader to only read caption data from an MXF asset file's essence
 */
public class MxfAncReader extends MxfReader {
    private final List<EssenceTrack> tracks;
    private final MxfView view;
    private boolean decodeCea708;
    private boolean decodeDolbyE;
    private boolean decodeAncillaryPackets;

    public MxfAncReader(File file, List<EssenceTrack> tracks, MxfView view) {
        super(file);
        this.tracks = tracks;
        this.view = view;
    }

    public boolean isDecodeCea708() {
        return decodeCea708;
    }

    public void setDecodeCea708(boolean decodeCea708) {
        this.decodeCea708 = decodeCea708;
    }

    public boolean isDecodeDolbyE() {
        return decodeDolbyE;
    }

    public void setDecodeDolbyE(boolean decodeDolbyE) {
        this.decodeDolbyE = decodeDolbyE;
    }

    public boolean isDecodeAncillaryPackets() {
        return decodeAncillaryPackets;
    }

    public void setDecodeAncillaryPackets(boolean decodeAncillaryPackets) {
        this.decodeAncillaryPackets = decodeAncillaryPackets;
    }

    @Override
    protected EssenceContainerOutputController createEssenceFileOutputController(File file) {
        return new AncEssenceOutput(this, tracks, view);
    }

    @Override
    protected void debug(String message) {
        view.debug(message);
    }

    @Override
    protected void log(String message) {
        view.info(message);
    }

    @Override
    protected void warn(String message) {
        view.warn(message);
    }

    @Override
    protected void error(String message, Exception exception) {
        view.error(message, exception);
    }
}
