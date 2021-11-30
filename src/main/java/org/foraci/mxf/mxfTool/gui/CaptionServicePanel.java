package org.foraci.mxf.mxfTool.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel to sink a text input stream
 */
public class CaptionServicePanel extends JPanel implements MxfViewListener {
    private JTextArea textArea;

    public CaptionServicePanel() {
        super(new BorderLayout());
        createUI();
    }

    private void createUI() {
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setTabSize(4);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    public void addText(String text) {
        textArea.append(text);
    }

    public void assetStartLoad() {
        textArea.setText("");
    }

    public void assetLoaded() {
    }
}
