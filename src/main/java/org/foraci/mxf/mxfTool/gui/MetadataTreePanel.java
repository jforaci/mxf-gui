package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.LeafNode;
import org.foraci.mxf.mxfReader.entities.Node;
import org.foraci.mxf.mxfReader.registries.Groups;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Displays the metadata structure in the form of an interactive tree
 */
public class MetadataTreePanel extends JPanel implements MxfViewListener
{
    /**
     * A <code>TreeNode</code> for a metadata key
     */
    private static class MetadataTreeNode extends DefaultMutableTreeNode
    {
        private final Node mdNode;

        MetadataTreeNode(Node mdNode) {
            this.mdNode = mdNode;
        }

        @Override
        public String toString() {
            String name = mdNode.ul().getName();
            if (name == null) {
                name = "[Unknown Label]";
            }
            if (mdNode instanceof LeafNode) {
                LeafNode leaf = (LeafNode) mdNode;
                String stringValue;
                if (leaf.values().isEmpty()) {
                    stringValue = "[Empty]";
                } else if (leaf.values().size() == 1) {
                    stringValue = leaf.values().get(0).toString();
                } else {
                    stringValue = leaf.values().toString();
                }
                if (mdNode.ul().getParserClass() == null) {
                    return String.format("%s (%d bytes): %s", name, mdNode.key().getLength(), stringValue);
                } else if (mdNode.ul().isStrongReference()) {
                    return String.format("%s <reference>", name);
                } else {
                    return String.format("%s: %s", name, stringValue);
                }
            } else {
                return name;
            }
        }
    }

    private MxfView view;
    private JTree tree;
    private DefaultMutableTreeNode root;

    public MetadataTreePanel(MxfView view) {
        super(new BorderLayout());
        this.view = view;
        this.root = new DefaultMutableTreeNode();
        createUI();
    }

    private void createUI() {
        tree = createStructureTree();
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    private JTree createStructureTree() {
        JTree tree = new JTree(root);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
//        tree.addTreeSelectionListener(this);
        return tree;
    }

    public void assetStartLoad() {
        root.removeAllChildren();
    }

    public void assetLoaded() {
        GroupNode preface = (GroupNode) view.getRootGroupNode().find(Groups.Preface);
        buildTree(view.getRootGroupNode(), root, preface);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.nodeStructureChanged(root);
        expandStructureTree();
    }

    private void buildTree(GroupNode rootGroupNode, DefaultMutableTreeNode rootTreeNode, GroupNode primaryChild) {
        Stack<GroupNode> nodeStack = new Stack<GroupNode>();
        Stack<DefaultMutableTreeNode> parentStack = new Stack<DefaultMutableTreeNode>();
        for (Node child : rootGroupNode.getChildren()) {
            if (child == primaryChild
                    || !(child instanceof GroupNode)) { //TODO: maybe allow non-set children, but filter noise like Filler Data
                continue;
            }
            nodeStack.push((GroupNode) child);
            parentStack.push(rootTreeNode);
        }
        if (primaryChild != null) {
            nodeStack.push(primaryChild);
            parentStack.push(rootTreeNode);
        }
        Set<GroupNode> resolvedGroups = new HashSet<GroupNode>();
        while (!nodeStack.isEmpty()) {
            GroupNode group = nodeStack.pop();
            DefaultMutableTreeNode parent = parentStack.pop();
            if (resolvedGroups.contains(group)) {
                continue;
            }
            resolvedGroups.add(group);
            final MetadataTreeNode groupTreeNode = new MetadataTreeNode(group);
            parent.add(groupTreeNode);
            List<Node> order = new ArrayList<Node>();
            for (Node child : group.getChildren()) {
                if (child instanceof LeafNode) {
                    LeafNode leaf = (LeafNode) child;
                    if (leaf.refs() == null) {
                        order.add(leaf);
                    }
                }
            }
            for (Node child : group.getChildren()) {
                if (child instanceof LeafNode) {
                    LeafNode leaf = (LeafNode) child;
                    if (leaf.refs() != null) {
                        order.add(leaf);
                    }
                }
            }
            for (Node child : group.getChildren()) {
                if (child instanceof GroupNode) {
                    order.add(child);
                }
            }
            for (Node child : order) {
                if (child instanceof LeafNode) {
                    LeafNode leaf = (LeafNode) child;
                    final MetadataTreeNode leafTreeNode = new MetadataTreeNode(leaf);
                    groupTreeNode.add(leafTreeNode);
                    if (leaf.refs() != null) {
                        for (ListIterator<GroupNode> i
                                = leaf.refs().listIterator(leaf.refs().size()); i.hasPrevious();) {
                            GroupNode r = i.previous();
                            nodeStack.push(r);
                            parentStack.push(leafTreeNode);
                        }
                    }
                } else {
                    nodeStack.push((GroupNode) child);
                    parentStack.push(groupTreeNode);
                }
            }
        }
    }

    private void expandStructureTree() {
        TreePath path = new TreePath(root);
        tree.expandPath(path);
        for (Enumeration<TreeNode> e = root.children(); e.hasMoreElements();) {
            TreeNode node = e.nextElement();
            tree.expandPath(path.pathByAddingChild(node));
        }
    }
}
