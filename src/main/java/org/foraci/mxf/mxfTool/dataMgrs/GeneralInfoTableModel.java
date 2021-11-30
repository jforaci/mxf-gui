package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.LeafNode;
import org.foraci.mxf.mxfReader.registries.Metadata;

import java.util.Iterator;

/**
 * Table model for the general "Info" tab
 */
public class GeneralInfoTableModel extends ContextTableModel {
    @Override
    protected void addLeaf(LeafNode leaf) {
        UL ul = leaf.ul();
        if (Metadata.Content.equals(ul)
                || Metadata.IdentificationList.equals(ul)) {
            for (Iterator<GroupNode> j = leaf.refs().iterator(); j.hasNext();) {
                GroupNode content = j.next();
                addRow(new Object[] { friendlyName(leaf), null }); // add header row
                loadFromGroupNode(content);
            }
        } else if (Metadata.EssenceContainers.equals(ul)) {
            super.addLeaf(leaf, true);
        } else {
            super.addLeaf(leaf);
        }
    }
}
