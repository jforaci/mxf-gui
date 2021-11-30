package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.mxf.mxfReader.entities.GroupNode;

/**
 * A node representing MXF structural metadata set
 */
public class SMNode {
    private GroupNode group;
    private String label;

    public SMNode(GroupNode group, String label) {
        this.group = group;
        this.label = label;
    }

    public GroupNode getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return label;
    }
}
