package org.foraci.mxf.mxfTool.gui.updaters;

import org.foraci.mxf.mxfTool.gui.LoggerPanel;

/**
 * Shows the latest error in the <code>LoggerPanel</code>
 */
public class ShowLoggerErrorRunnable implements Runnable
{
    private final LoggerPanel loggerPanel;

    public ShowLoggerErrorRunnable(LoggerPanel loggerPanel) {
        this.loggerPanel = loggerPanel;
    }

    public void run() {
        loggerPanel.scrollToLatest();
    }
}
