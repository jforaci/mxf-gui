package org.foraci.mxf.mxfTool.dataMgrs;

import javax.swing.table.DefaultTableModel;

/**
 * Table model for the keys within a set
 */
public class CustomTableModel extends DefaultTableModel {
    public CustomTableModel(Object[] columnNames) {
        super(columnNames, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
