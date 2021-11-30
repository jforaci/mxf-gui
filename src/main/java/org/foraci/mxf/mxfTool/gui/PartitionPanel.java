package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfReader.PartitionPack;
import org.foraci.mxf.mxfTool.dataMgrs.PartitionsTableModel;

import javax.swing.*;
import java.awt.*;

/**
 * Shows general information about an asset when it's loaded
 */
public class PartitionPanel extends JPanel implements MxfViewListener {
    private MxfView view;
    private JTable table;

    public PartitionPanel(MxfView view) {
        super(new BorderLayout());
        this.view = view;
        createUI();
    }

    private void createUI() {
        PartitionsTableModel model = new PartitionsTableModel();
        table = new JTable(model);
        CustomTableCellRenderer r = new CustomTableCellRenderer();
        table.setTableHeader(null);
        table.setDefaultRenderer(Object.class, r);
        table.setShowGrid(false);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void assetStartLoad() {
    }

    public void assetLoaded() {
        PartitionsTableModel model = (PartitionsTableModel) table.getModel();
        model.setRowCount(0);
        for (PartitionPack partitionPack : view.getPartitionPackList()) {
            model.loadFromPartitionPack(partitionPack);
        }
        if (view.getRandomIndexPack() != null) {
            model.addRow(new Object[] { "Random Index Pack found", null });
        } else {
            model.addRow(new Object[] { "No RIP found", null });
        }
        table.setModel(model);
    }
}
