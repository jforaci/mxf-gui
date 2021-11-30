package org.foraci.mxf.mxfTool.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * A table cell renderer for context tables
 */
public class CustomTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//        if (isSelected) {
//            return c;
//        }
        if (column == 0) {
            value = table.getModel().getValueAt(row, 1);
        }
        if (value == null) {
            if (!isSelected) {
                c.setBackground(UIManager.getColor("ToolTip.background"));
            }
            c.setFont(c.getFont().deriveFont(Font.BOLD));
//            JLabel label = (JLabel) c;
        } else {
            if (!isSelected) {
                c.setBackground(Color.WHITE);
            }
        }
        if (!(c instanceof JLabel)) {
        }
        return c;
    }
}
