package org.foraci.mxf.mxfTool.dnd;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;

/**
 * A <code>TransferHandler</code> that uses a <code>FileSelection</code> to copy
 * a list of <code>File</code>s
 *
 * @author jforaci
 */
public class FileCopyTransferHandler extends TransferHandler
{
    private int columnIndex;

    public FileCopyTransferHandler(int columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    /**
     * Bundle up the data for export.
     */
    protected Transferable createTransferable(JComponent c)
    {
        JTable table = (JTable)c;
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            return null;
        }
        TableModel model = table.getModel();
        ArrayList<File> files = new ArrayList<File>(rows.length);
        for (int row : rows) {
            File file = (File)model.getValueAt(row, columnIndex);
            files.add(file);
        }
        return new FileSelection(files);
    }

    /**
     * The list handles both copy and move actions.
     */
    public int getSourceActions(JComponent c)
    {
        return COPY;
    }

    /**
     * When the export is complete, remove the old list entry if the
     * action was a move.
     */
    protected void exportDone(JComponent c, Transferable data, int action)
    {
    }
}
