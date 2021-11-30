package org.foraci.mxf.mxfTool.gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * CC panel
 */
public class CaptionPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private Map<Integer, CaptionServicePanel> componentMap
            = new HashMap<Integer, CaptionServicePanel>();

    public CaptionPanel() {
        super(new BorderLayout());
        createUI();
    }

    private void createUI() {
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    public CaptionServicePanel getServicePanel(int serviceNumber) {
        CaptionServicePanel captionServicePanel;
        if (componentMap.containsKey(serviceNumber)) {
            captionServicePanel = componentMap.get(serviceNumber);
            return captionServicePanel;
        }
        captionServicePanel = new CaptionServicePanel();
        String tabName = "Service " + serviceNumber;
        int position = findBestTabPosition(tabName);
        tabbedPane.insertTab(tabName, null, captionServicePanel, null, position);
        componentMap.put(serviceNumber, captionServicePanel);
        return captionServicePanel;
    }

    private int findBestTabPosition(String tabName) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).compareTo(tabName) > 0) {
                return i;
            }
        }
        return tabbedPane.getTabCount();
    }

    public void assetStartLoad() {
        tabbedPane.removeAll();
        componentMap.clear();
    }

    public void assetLoaded() {
        for (Iterator<CaptionServicePanel> i = componentMap.values().iterator(); i.hasNext();) {
            i.next().assetLoaded();
        }
    }
}
