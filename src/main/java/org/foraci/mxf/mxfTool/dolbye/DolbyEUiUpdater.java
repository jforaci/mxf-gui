package org.foraci.mxf.mxfTool.dolbye;

import org.foraci.mxf.mxfTool.dataMgrs.EssenceTrack;
import org.foraci.mxf.mxfTool.gui.MxfView;

import java.util.List;

/**
 * Updates the user interface with info about Dolby E gathered by
 * <code>DolbyEConsumer</code>
 */
public class DolbyEUiUpdater implements Runnable {
    private final MxfView view;
    private final EssenceTrack track;
    private final List<String> messages;

    DolbyEUiUpdater(MxfView view, EssenceTrack track, List<String> messages) {
        this.view = view;
        this.track = track;
        this.messages = messages;
    }

    public void run() {
        for (String message : messages) {
            view.getDolbyEInfoPanel(track).addText(message);
        }
    }
}
