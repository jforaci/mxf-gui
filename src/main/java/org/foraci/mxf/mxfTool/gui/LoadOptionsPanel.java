package org.foraci.mxf.mxfTool.gui;

import javax.swing.*;
import java.awt.*;

/**
 * UI to display file loading options
 */
public class LoadOptionsPanel extends JPanel {
    private JCheckBox loadCaptionsCheckBox;
    private JCheckBox loadDolbyECheckBox;
    private JCheckBox followExternalReferencesCheckBox;
    private JCheckBox readFromFooter;
    private JCheckBox ignoreCheckBox;

    public LoadOptionsPanel() {
        super(new GridLayout(4, 1));
        createUI();
    }

    private void createUI() {
        setBorder(BorderFactory.createTitledBorder("Load Options"));
        loadCaptionsCheckBox = new JCheckBox("Decode CEA 708 (436m) -- SLOW", false);
        add(loadCaptionsCheckBox);
        loadDolbyECheckBox = new JCheckBox("Decode Dolby E", false);
        add(loadDolbyECheckBox);
        followExternalReferencesCheckBox = new JCheckBox("Follow external references", true);
        add(followExternalReferencesCheckBox);
        readFromFooter = new JCheckBox("Rewind from footer", false);
        add(readFromFooter);
    }

    public boolean isDecoding708() {
        return loadCaptionsCheckBox.isSelected();
    }

    public boolean isDecodingDolbyE() {
        return loadDolbyECheckBox.isSelected();
    }

    public boolean isFollowExternalReferences() {
        return followExternalReferencesCheckBox.isSelected();
    }

    public boolean isReadingFromFooter() {
        return readFromFooter.isSelected();
    }
}
