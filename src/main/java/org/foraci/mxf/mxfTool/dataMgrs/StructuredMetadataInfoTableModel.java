package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.LeafNode;
import org.foraci.mxf.mxfReader.entities.Node;
import org.foraci.mxf.mxfReader.registries.Metadata;

import java.util.Iterator;

/**
 * Table model for the list shown when a node is selected in the structural metadata tree
 */
public class StructuredMetadataInfoTableModel extends ContextTableModel {
    @Override
    protected void addLeaf(LeafNode leaf) {
        UL ul = leaf.ul();
        // sequence
        if (Metadata.Segment.equals(ul)) {
            for (Iterator<GroupNode> j = leaf.refs().iterator(); j.hasNext();) {
                GroupNode segment = j.next();
                for (Iterator<Node> i = segment.getChildren().iterator(); i.hasNext();) {
                    Node node = i.next();
                    ul = node.ul();
                    if (Metadata.ComponentLength.equals(ul)) {
                        super.addLeaf((LeafNode) node);
                    } else if (Metadata.ComponentDataDefinition.equals(ul)) {
                        super.addLeaf((LeafNode) node);
                    } else if (Metadata.ComponentsinSequence.equals(ul)) {
                        super.addLeaf((LeafNode) node);
                    }
                }
            }
//        } else if (Metadata.EssenceDescription.equals(ul)) {
//            for (Iterator<GroupNode> j = leaf.refs().iterator(); j.hasNext();) {
//                GroupNode desc = j.next();
//                addRow(new Object[] { desc.ul().getName(), null });
//                loadFromGroupNode(desc, true); // true for Multiple Descriptor
//            }
//        } else if (Metadata.EssenceLocators.equals(ul)) {
//            for (Iterator<GroupNode> j = leaf.refs().iterator(); j.hasNext();) {
//                GroupNode loc = j.next();
//                addRow(new Object[] { loc.ul().getName(), null });
//                loadFromGroupNode(loc);
//            }
        } else {
            super.addLeaf(leaf);
        }
    }
}
