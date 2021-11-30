package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.LeafNode;
import org.foraci.mxf.mxfReader.entities.Node;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfReader.registries.Metadata;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Table model for the keys within a set
 */
public class ContextTableModel extends DefaultTableModel {
    public ContextTableModel() {
        super(new Object[] { "Name", "Value" }, 0);
    }

    public void loadFromGroupNode(GroupNode group) {
        loadFromGroupNode(group, false);
    }

    public void loadFromGroupNode(GroupNode group, boolean followReferences) {
//        addRow(new Object[] { group.getKey().getUL().getName(), null }); // add header row
        List<GroupNode> groups = new ArrayList<GroupNode>();
        for (Iterator<Node> i = group.getChildren().iterator(); i.hasNext();) {
            Node child = i.next();
            if (Metadata.GUID.equals(child.ul())) {
                continue;
            }
            if (child instanceof LeafNode) {
                if (followReferences && ((LeafNode) child).refs() != null) {
                    groups.addAll(((LeafNode) child).refs());
                } else {
                    addLeaf((LeafNode) child);
                }
            } else {
                groups.add((GroupNode) child);
            }
        }
        for (GroupNode childGroup : groups) {
            addRow(new Object[] { childGroup.ul().getName(), null });
            loadFromGroupNode(childGroup, false);
//            addRow(new Object[] { "End of " + childGroup.ul().getName(), null });
        }
    }

    protected void addLeaf(LeafNode leaf) {
        addLeaf(leaf, false);
    }

    protected void addLeaf(LeafNode leaf, boolean linePerValue) {
        if (!linePerValue || leaf.values().size() < 2) {
            addRow(new Object[] { friendlyName(leaf), friendlyValue(leaf, leaf.values()) });
        } else {
            addRow(new Object[] { friendlyName(leaf), null });
            for (Object value : leaf.values()) {
                addRow(new Object[] { null, friendlyValue(leaf, Collections.singletonList(value)) });
            }
        }
    }

    protected String friendlyName(LeafNode leaf) {
        UL ul = leaf.ul();
        if (Metadata.OperationalPatternUL.equals(ul)) {
            return "Operational Pattern";
        } else if (Metadata.CreationDateTime.equals(ul)) {
            return "Creation Date";
        } else if (Metadata.PackageLastModificationDateTime.equals(ul)) {
            return "Modified Date";
        } else if (Metadata.ContainerLastModificationDateTime.equals(ul)) {
            return "Last Modification";
        } else if (Metadata.IdentificationList.equals(ul)) {
            return "Identification";
        } else if (Metadata.TrackID.equals(ul)) {
            return "Track ID";
        } else if (Metadata.TrackName.equals(ul) || Metadata.TrackName1.equals(ul)) {
            return "Track Name";
        } else if (Metadata.TrackNumber.equals(ul)) {
            return "Track Number";
        } else if (Metadata.TimelineEditRate.equals(ul)) {
            return "Edit Rate";
        } else if (Metadata.ComponentDataDefinition.equals(ul)) {
            return "Data Definition";
        } else if (Metadata.ComponentLength.equals(ul)) {
            return "Duration";
        } else if (Metadata.ComponentsinSequence.equals(ul)) {
            return "Components";
        } else if (Metadata.ApplicationSupplierName.equals(ul)
                || Metadata.ApplicationSupplierName1.equals(ul)) {
            return "Application Supplier";
        } else if (Metadata.ApplicationName.equals(ul)
                || Metadata.ApplicationName1.equals(ul)) {
            return "Application";
        } else if (Metadata.ApplicationVersionString.equals(ul)
                || Metadata.ApplicationVersionString1.equals(ul)) {
            return "Application Version";
        } else if (Metadata.ApplicationPlatform.equals(ul)
                || Metadata.ApplicationPlatform1.equals(ul)) {
            return "Application Platform";
        } else if (Metadata.ApplicationProductID.equals(ul)) {
            return "Application Product ID";
        } else if (Metadata.ModificationDateTime.equals(ul)) {
            return "Modified";
        } else if (Metadata.URL.equals(ul) || Metadata.URL1.equals(ul)) {
            return "URL";
        }
        return ul.getName();
    }

    protected String friendlyValue(LeafNode leaf, List values) {
        if (values.isEmpty()) {
            return "<empty>";
        }
        UL ul = leaf.ul();
        if (Metadata.Tracks.equals(ul)) {
            return Integer.toString(leaf.values().size()) + " tracks";
        } else if (Metadata.ComponentsinSequence.equals(ul)) {
            return Integer.toString(leaf.values().size()) + " clips";
        } else if (Metadata.Packages.equals(ul)) {
            return getPackagesDescription(leaf);
        }
        StringBuffer sb = new StringBuffer();
        for (Iterator i = values.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof UL) {
                UL label = (UL)o;
                sb.append((label.getName() != null) ? label.getName() : "<unknown label>");
            } else {
                sb.append(o.toString());
            }
            if (i.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private String getPackagesDescription(LeafNode leaf) {
        int numMaterialPkgs = 0;
        int numSourcePkgs = 0;
        int numUnknown = 0;
        for (Iterator<GroupNode> g = leaf.refs().iterator(); g.hasNext();) {
            GroupNode group = g.next();
            UL ul1 = group.ul();
            if (Groups.MaterialPackage.equals(ul1)) {
                numMaterialPkgs++;
            } else if (Groups.SourcePackage.equals(ul1)) {
                numSourcePkgs++;
            } else {
                numUnknown++;
            }
        }
        return "" + numMaterialPkgs + " material, "
                + numSourcePkgs + " source, "
                + numUnknown + " unknown";
    }

    protected void addGroup(GroupNode group) {
        addRow(new Object[] { group.ul().getName(), "<Group>" });
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
