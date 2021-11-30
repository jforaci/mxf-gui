package org.foraci.mxf.mxfTool.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel to sink a text input stream
 */
public class DolbyETrackInfoPanel extends JPanel implements MxfViewListener {
    private JTextArea textArea;

    public DolbyETrackInfoPanel() {
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
        textArea.append(text + "\n");
    }

    public void assetStartLoad() {
        // never called; these panels are created as needed
    }

    public void assetLoaded() {
    }
}
