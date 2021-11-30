package org.foraci.mxf.mxfTool.gui.renderers;

import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfTool.dataMgrs.SMNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Renderer for the structural metadata tree
 */
public class StructuralTreeCellRenderer extends DefaultTreeCellRenderer
{
    public static final Icon STATIC_TRACK_ICON;
    public static final Icon TIMELINE_TRACK_ICON;
    public static final Icon DESCRIPTOR_ICON;

    static {
        TIMELINE_TRACK_ICON = new ImageIcon(StructuralTreeCellRenderer.class.getResource(
                "/org/foraci/mxf/mxfTool/res/timeline-track.png"));
        STATIC_TRACK_ICON = new ImageIcon(StructuralTreeCellRenderer.class.getResource(
                "/org/foraci/mxf/mxfTool/res/static-track.png"));
        DESCRIPTOR_ICON = new ImageIcon(StructuralTreeCellRenderer.class.getResource(
                "/org/foraci/mxf/mxfTool/res/descriptor.png"));
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
        Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (!(value instanceof DefaultMutableTreeNode)) {
            return c;
        }
        if (!(c instanceof JLabel)) {
            return c;
        }
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        Object userObject = treeNode.getUserObject();
        if (!(userObject instanceof SMNode)) {
            return c;
        }
        SMNode smNode = (SMNode) userObject;
        JLabel label = (JLabel) c;
        GroupNode groupNode = smNode.getGroup();
        if (Groups.TimelineTrack.equals(groupNode.ul())) {
            label.setIcon(TIMELINE_TRACK_ICON);
        } else if (Groups.StaticTrack.equals(groupNode.ul())) {
            label.setIcon(STATIC_TRACK_ICON);
        } else if (Groups.GenericPictureEssenceDescriptor.equals(groupNode.ul())
                || Groups.Mpeg2VideoDescriptor.equals(groupNode.ul())
                || Groups.Jpeg2kSubDescriptor.equals(groupNode.ul())
                || Groups.RGBAEssenceDescriptor.equals(groupNode.ul())
                || Groups.CDCIEssenceDescriptor.equals(groupNode.ul())) {
            label.setIcon(DESCRIPTOR_ICON);
        } else if (Groups.GenericSoundEssenceDescriptor.equals(groupNode.ul())
                || Groups.Aes3Descriptor.equals(groupNode.ul())
                || Groups.WaveAudioEssenceDescriptor.equals(groupNode.ul())
                || Groups.WaveAudioPhysicalDescriptor.equals(groupNode.ul())) {
            label.setIcon(DESCRIPTOR_ICON);
        } else if (Groups.AncPacketsDescriptor.equals(groupNode.ul())
                || Groups.VbiDataDescriptor.equals(groupNode.ul())) {
            label.setIcon(DESCRIPTOR_ICON);
        }
        return label;
    }
}
