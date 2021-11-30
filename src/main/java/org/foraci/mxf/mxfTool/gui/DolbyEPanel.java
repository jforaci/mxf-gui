package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfReader.registries.Metadata;
import org.foraci.mxf.mxfTool.dataMgrs.EssenceTrack;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Dolby E panel
 */
public class DolbyEPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private Map<EssenceTrack, DolbyETrackInfoPanel> componentMap
            = new HashMap<EssenceTrack, DolbyETrackInfoPanel>();

    public DolbyEPanel() {
        super(new BorderLayout());
        createUI();
    }

    private void createUI() {
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    public DolbyETrackInfoPanel getTrackPanel(EssenceTrack track) {
        DolbyETrackInfoPanel trackInfoPanel;
        if (componentMap.containsKey(track)) {
            trackInfoPanel = componentMap.get(track);
            return trackInfoPanel;
        }
        trackInfoPanel = new DolbyETrackInfoPanel();
        long trackId = ((Number) track.getTrack().value(Metadata.TrackID)).longValue();
        String tabName = "Track ID " + trackId;
        int position = findBestTabPosition(tabName);
        tabbedPane.insertTab(tabName, null, trackInfoPanel, null, position);
        componentMap.put(track, trackInfoPanel);
        return trackInfoPanel;
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
        for (Iterator<DolbyETrackInfoPanel> i = componentMap.values().iterator(); i.hasNext();) {
            i.next().assetLoaded();
        }
    }
}
