package org.foraci.mxf.mxfTool.dolbye;

import org.foraci.dolby.DolbyEReader;
import org.foraci.anc.util.io.MultiplexingInputStream;
import org.foraci.mxf.mxfTool.dataMgrs.EssenceTrack;
import org.foraci.mxf.mxfTool.gui.MxfView;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Dolby E sink to read the first frame of E (if it's present) and then exit
 */
public class DolbyEConsumer extends DolbyEReader implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(DolbyEConsumer.class.getName());

    private MultiplexingInputStream in;
    private final MxfView view;
    private final boolean align;
    private final boolean probe;
    private final EssenceTrack track;
    private List<String> messages;
    private volatile boolean done;
    private volatile boolean kill;

    public DolbyEConsumer(MultiplexingInputStream in, MxfView view,
                   int sampleSize, boolean align, boolean probe, EssenceTrack track) {
        super(in, sampleSize, false);
        this.in = in;
        this.view = view;
        this.align = align;
        this.probe = probe;
        this.track = track;
        this.messages = new ArrayList<String>();
    }

    public void run() {
        log.info("Looking for AES/DolbyE on " + track);
        try {
            if (align) {
                in.align();
            }
            if (probe) {
                probeForAESFrame(1024 * 1024);
            }
            readFrame();
            log.info("Found DolbyE on " + track);
        } catch (Exception e) {
            messages.add("Failed to find DolbyE on " + track + ": " + e.getMessage());
            log.info("Failed to find DolbyE on " + track + ": " + e.getMessage());
        }
        done = true;
        SwingUtilities.invokeLater(createUiUpdater());
        log.debug("sinking rest of stream for " + track);
        try {
            while (in.skip(1024*1024) > 0);
            while (!kill) {
                synchronized (this) {
                    wait();
                }
            }
            log.debug("consumer for " + track + " is terminating");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private DolbyEUiUpdater createUiUpdater() {
        return new DolbyEUiUpdater(view, track, messages);
    }

    public boolean isDone() {
        return done;
    }

    public void kill()
    {
        this.kill = true;
    }

    @Override
    protected void info(String message) {
        messages.add(message);
    }

    @Override
    protected void warn(String message) {
        messages.add("WARN: " + message);
    }
}
