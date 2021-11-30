package org.foraci.mxf.mxfTool.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * A table cell renderer for context tables
 */
public class LoggerTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            return c;
        }
        JLabel label = (JLabel) c;
        value = table.getModel().getValueAt(row, 1);
        label.setBackground(Color.WHITE);
        if ("ERROR".equals(value)) {
//                label.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
            label.setForeground(Color.WHITE);
            label.setBackground(Color.RED);
        } else if ("WARN".equals(value)) {
//                label.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
            label.setForeground(Color.WHITE);
            label.setBackground(Color.ORANGE);
        } else if ("INFO".equals(value)) {
//                label.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
            label.setForeground(Color.BLACK);
        } else if ("DEBUG".equals(value)) {
//                label.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
            label.setForeground(Color.BLACK);
        } else {
            label.setForeground(Color.BLACK);
        }
        return c;
    }
}
