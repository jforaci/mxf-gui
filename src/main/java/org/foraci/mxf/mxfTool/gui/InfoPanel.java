package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.Node;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfTool.dataMgrs.ContentPackageSignature;
import org.foraci.mxf.mxfTool.dataMgrs.GeneralInfoTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

/**
 * Shows general information about an asset when it's loaded
 */
public class InfoPanel extends JPanel implements MxfViewListener {
    private MxfView view;
    private JTable table;

    public InfoPanel(MxfView view) {
        super(new BorderLayout());
        this.view = view;
        createUI();
    }

    private void createUI() {
        GeneralInfoTableModel model = new GeneralInfoTableModel();
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
        GeneralInfoTableModel model = (GeneralInfoTableModel) table.getModel();
        model.setRowCount(0);
        GroupNode root = view.getRootGroupNode();
        for (Iterator<Node> i = root.getChildren().iterator(); i.hasNext();) {
            Node node = i.next();
            if (Groups.Preface.equals(node.ul())) {
                model.addRow(new Object[] { node.ul().getName(), null }); // add header row
                model.loadFromGroupNode((GroupNode) node);
            }
        }
        // add Essence Container Content Package signature header row
        model.addRow(new Object[] { "Content Package signature", null });
        int count = 1;
        for (Iterator<ContentPackageSignature> i
             = view.getEssenceController().getSignatures().iterator(); i.hasNext();) {
            ContentPackageSignature cpSignature = i.next();
            String samestr = (cpSignature.getOccurrences() > 0)
                    ? " (x" + cpSignature.getOccurrences() + ")" : "";
            model.addRow(new Object[] { "CP #" + count + samestr, cpSignature.toString() });
            count += cpSignature.getOccurrences();
        }
        table.setModel(model);
    }
}
