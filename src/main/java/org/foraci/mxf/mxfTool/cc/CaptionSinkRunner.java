package org.foraci.mxf.mxfTool.cc;

import org.foraci.anc.cc.CaptionsSink;
import org.foraci.mxf.mxfTool.gui.MxfView;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

/**
 * Input sink
 */
public class CaptionSinkRunner implements CaptionsSink, Runnable {
    private MxfView view;
    private Map<Integer, List<String>> messageMap;

    public CaptionSinkRunner(MxfView view) {
        this.view = view;
        this.messageMap = new HashMap<Integer, List<String>>();
    }

    public synchronized void run() {
        for (Iterator<Integer> i = messageMap.keySet().iterator(); i.hasNext();) {
            int serviceNumber = i.next();
            List<String> messages = messageMap.get(serviceNumber);
            for (String message : messages) {
                view.getCaptionServicePanel(serviceNumber).addText(message + "\n");
            }
            messages.clear();
        }
    }

    public synchronized void write(char[] cbuf, int off, int len, int serviceNumber) throws IOException {
        String text = String.valueOf(cbuf, off, len);
        List<String> messages = messageMap.get(serviceNumber);
        if (messages == null) {
            messages = new ArrayList<String>();
            messageMap.put(serviceNumber, messages);
        }
        messages.add(text);
        SwingUtilities.invokeLater(this);
    }
}
