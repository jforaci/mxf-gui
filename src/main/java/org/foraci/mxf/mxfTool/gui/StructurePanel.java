package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfTool.dataMgrs.SMNode;
import org.foraci.mxf.mxfTool.dataMgrs.StructuredMetadataInfoTableModel;
import org.foraci.mxf.mxfTool.gui.renderers.StructuralTreeCellRenderer;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;

/**
 * Shows structural metadata
 */
public class StructurePanel extends JPanel implements MxfViewListener, TreeSelectionListener {
    private MxfView view;
    private JSplitPane splitPane;
    private JTree structTree;
    private JTable structureContextList;

    public StructurePanel(MxfView view) {
        super(new BorderLayout());
        this.view = view;
        createUI();
    }

    private void createUI() {
//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        structTree = createStructureTree();
//        add(new JScrollPane(structTree));
        structureContextList = createStructureContextList();
//        add(new JScrollPane(structureContextList));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane(structTree), new JScrollPane(structureContextList));
        add(new JLabel("Structural Metadata:"), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private JTable createStructureContextList() {
        StructuredMetadataInfoTableModel model = new StructuredMetadataInfoTableModel();
        JTable table = new JTable(model);
        CustomTableCellRenderer r = new CustomTableCellRenderer();
        table.setDefaultRenderer(Object.class, r);
        table.setShowGrid(false);
        return table;
    }

    private JTree createStructureTree() {
        JTree tree = new JTree(view.getStructureTreeRoot());
        StructuralTreeCellRenderer renderer = new StructuralTreeCellRenderer();
        tree.setCellRenderer(renderer);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.addTreeSelectionListener(this);
//        FileDropTransferHandler transferHandler = new FileDropTransferHandler(this);
//        tree.setTransferHandler(transferHandler);
        return tree;
    }

    public void assetStartLoad() {
        view.getStructureTreeRoot().removeAllChildren();
    }

    public void assetLoaded() {
        splitPane.setDividerLocation(0.3);
//        structTree.setModel(new DefaultTreeModel(view.getStructureTreeRoot()));
        DefaultTreeModel model = (DefaultTreeModel) structTree.getModel();
        model.nodeStructureChanged(view.getStructureTreeRoot());
        expandStructureTree();
        selectFirstMaterialPackageNode();
    }

    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        SMNode userNode = (SMNode) node.getUserObject();
        GroupNode group = userNode.getGroup();
        StructuredMetadataInfoTableModel model = (StructuredMetadataInfoTableModel) structureContextList.getModel();
        model.setRowCount(0);
        model.loadFromGroupNode(group, true);
        structureContextList.setModel(model);
//        structureContextList.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        structureContextList.getColumnModel().getColumn(0).setPreferredWidth(50);
//        structureContextList.getColumnModel().getColumn(0).setMaxWidth(125);
    }

    private void expandStructureTree() {
        TreePath path = new TreePath(view.getStructureTreeRoot());
        structTree.expandPath(path);
        for (Enumeration<TreeNode> e = view.getStructureTreeRoot().children(); e.hasMoreElements();) {
            TreeNode node = e.nextElement();
            structTree.expandPath(path.pathByAddingChild(node));
        }
    }

    private void selectFirstMaterialPackageNode() {
        TreePath path = new TreePath(view.getStructureTreeRoot());
        for (Enumeration<TreeNode> e = view.getStructureTreeRoot().children(); e.hasMoreElements();) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            SMNode userNode = (SMNode) node.getUserObject();
            GroupNode group = userNode.getGroup();
            if (Groups.MaterialPackage.equals(group.ul())) {
                structTree.setSelectionPath(path.pathByAddingChild(node));
                return;
            }
        }
    }
}
