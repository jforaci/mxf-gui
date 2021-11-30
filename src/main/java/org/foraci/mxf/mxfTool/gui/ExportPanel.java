package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfTool.dataMgrs.EssenceTrack;
import org.foraci.mxf.mxfTool.dataMgrs.export.ExportTrackTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A panel for export options
 */
public class ExportPanel extends JPanel {
    private static class TrackIncludeAction extends AbstractAction {
        private final ExportTrackTableModel model;
        private final boolean includeAll;

        private TrackIncludeAction(ExportTrackTableModel model, boolean includeAll) {
            super((includeAll) ? "Select All" : "Select None");
            this.model = model;
            this.includeAll = includeAll;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (includeAll) {
                model.includeAll();
            } else {
                model.includeNone();
            }
        }
    }

    private MxfView view;
    private ExportTrackTableModel model;
    private JCheckBox createCaptionOnlyFileCheckBox;
    private JCheckBox zeroBasedCaptionFileCheckBox;
    private JPopupMenu contextMenu;

    public ExportPanel(MxfView view) {
        super(null);
        this.view = view;
        createUI();
    }

    private void createUI() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        model = new ExportTrackTableModel(view.getExportableTrackList());
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getColumnModel().getColumn(0).setPreferredWidth(25);
        table.getColumnModel().getColumn(0).setMaxWidth(25);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(5);
        table.getColumnModel().getColumn(3).setPreferredWidth(5);
        table.getColumnModel().getColumn(4).setPreferredWidth(300);
        table.setPreferredScrollableViewportSize(new Dimension(200, 200));
        final JLabel label = new JLabel("<html>Choose which tracks to export (they'll be created in the same directory as the file with the essence data):");
        label.setAlignmentX(0);
        add(label);
        final JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setAlignmentX(0);
        add(scrollPane);
        contextMenu = new JPopupMenu();
        contextMenu.add(new JMenuItem(new TrackIncludeAction(model, true)));
        contextMenu.add(new JMenuItem(new TrackIncludeAction(model, false)));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        southPanel.setAlignmentX(0);
        southPanel.add(new JButton(new TrackIncludeAction(model, true)));
        southPanel.add(new JButton(new TrackIncludeAction(model, false)));
        createCaptionOnlyFileCheckBox = new JCheckBox("Create caption-only file instead of dumping 436m");
        southPanel.add(createCaptionOnlyFileCheckBox);
        zeroBasedCaptionFileCheckBox = new JCheckBox("Zero-base captions");
        southPanel.add(zeroBasedCaptionFileCheckBox);
        add(southPanel);
    }

    public boolean getCreateCaptionOnlyFile() {
        return createCaptionOnlyFileCheckBox.isSelected();
    }

    public boolean isCaptionFileZeroBased() { return zeroBasedCaptionFileCheckBox.isSelected(); }

    public List<EssenceTrack> getSelectedTracks() {
        return model.getSelectedTracks();
    }
}
