package org.foraci.mxf.mxfTool.mxf;

import org.foraci.mxf.mxfReader.*;
import org.foraci.mxf.mxfTool.dataMgrs.EssenceOutput;
import org.foraci.mxf.mxfTool.dataMgrs.EssenceTrack;
import org.foraci.mxf.mxfTool.gui.MxfView;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Reader to export MXF assets
 */
public class MxfExportReader extends MxfReader {
    private MxfView view;
    private List<EssenceTrack> tracks = null;
    private final boolean createCaptionOnlyFile;
    private final boolean zeroBasedCaptionFile;
    private EssenceOutput outputController;

    public MxfExportReader(File file, MxfView view, List<EssenceTrack> tracks,
                           boolean createCaptionOnlyFile, boolean zeroBasedCaptionFile) {
        super(file);
        this.view = view;
        this.tracks = tracks;
        this.createCaptionOnlyFile = createCaptionOnlyFile;
        this.zeroBasedCaptionFile = zeroBasedCaptionFile;
    }

    public List<EssenceTrack> getTracks() {
        return tracks;
    }

    @Override
    public void readAll() throws IOException {
        super.readAll();
    }

    @Override
    public void groupSetStarted(Key key) {
        super.groupSetStarted(key);
    }

    @Override
    public void groupSetEnded(Key key) {
        super.groupSetEnded(key);
    }

    @Override
    public void valueRead(Key key, Object value) {
        super.valueRead(key, value);
    }

    @Override
    protected EssenceContainerOutputController createEssenceFileOutputController(File file) {
        outputController = new EssenceOutput(file, view, tracks, createCaptionOnlyFile, zeroBasedCaptionFile);
        return outputController;
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
