package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfTool.dataMgrs.CustomTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A panel to show messages from the parser and other areas
 */
public class LoggerPanel extends JPanel implements MxfViewListener, MouseListener {
    private class ClearLogAction extends AbstractAction {
        ClearLogAction() {
            putValue(AbstractAction.NAME, "Clear Log");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            model.setNumRows(0);
            model.fireTableDataChanged();
        }
    }

    private CustomTableModel model;
    private JTable table;
    private JPopupMenu contextMenu;
    private DateFormat dateFormat;

    public LoggerPanel() {
        super(new BorderLayout());
        createUI();
    }

    private void createUI() {
        ClearLogAction clearLogAction = new ClearLogAction();
        contextMenu = new JPopupMenu("context");
        contextMenu.add(new JMenuItem(clearLogAction));
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        model = new CustomTableModel(new String[] { "Time", "Level", "Message" });
        table = new JTable(model);
        LoggerTableCellRenderer r = new LoggerTableCellRenderer();
        table.setDefaultRenderer(Object.class, r);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getColumnModel().getColumn(0).setPreferredWidth(125);
        table.getColumnModel().getColumn(0).setMaxWidth(125);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.addMouseListener(this);
//        table.getColumnModel().getColumn(2).setPreferredWidth(800);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void debug(String message) {
        model.addRow(new Object[] { dateFormat.format(now()), "DEBUG", message });
    }

    public void info(String message) {
        model.addRow(new Object[] { dateFormat.format(now()), "INFO", message });
    }

    public void warn(String message) {
        model.addRow(new Object[] { dateFormat.format(now()), "WARN", message });
    }

    public void error(String message, Exception e) {
        if (e != null) {
            message += ": " + e.getMessage();
        }
        model.addRow(new Object[] { dateFormat.format(now()), "ERROR", message });
    }

    private Date now() {
        return new Date(System.currentTimeMillis());
    }

    public void assetStartLoad() {
    }

    public void assetLoaded() {
        //model.fireTableDataChanged();
    }

    public void scrollToLatest() {
        table.scrollRectToVisible(new Rectangle(0, table.getHeight(), 1, 1));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        contextMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
