package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.mxf.mxfReader.PartitionPack;
import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.entities.PartitionPackKind;

import javax.swing.table.DefaultTableModel;

/**
 * Table model for the general "Info" tab
 */
public class PartitionsTableModel extends DefaultTableModel {
    public PartitionsTableModel() {
        super(new Object[] { "Name", "Value" }, 0);
    }

    public void loadFromPartitionPack(PartitionPack partitionPack) {
        addRow(new Object[] { "Partition", null }); // add header row
        addPartition(partitionPack);
    }

    private void addPartition(PartitionPack partitionPack) {
        addRow(new Object[] { "Type", getPartitionType(partitionPack.getKind()) });
        addRow(new Object[] { "Status", getPartitionStatus(partitionPack.getStatus()) });
        addRow(new Object[] { "Version", "Major:" + partitionPack.getMajorVersion()
                + ", minor: " + partitionPack.getMinorVersion() });
        addRow(new Object[] { "Operational Pattern", partitionPack.getOperationalPattern().getName() });
        addRow(new Object[] { "Header size (bytes)", partitionPack.getHeaderByteCount() });
        addRow(new Object[] { "Index size (bytes)", partitionPack.getIndexByteCount() });
        addRow(new Object[] { "Body Stream ID", partitionPack.getBodySid() });
        addRow(new Object[] { "Body Offset", partitionPack.getBodyOffset() });
        addRow(new Object[] { "Index Stream ID", partitionPack.getIndexSid() });
        addRow(new Object[] { "Offset from header", partitionPack.getThisPartition() });
        addRow(new Object[] { "Previous offset from header", partitionPack.getPreviousPartition() });
        addRow(new Object[] { "Footer offset from header", partitionPack.getFooterPartition() });
        addRow(new Object[] { "KLV Alignment size (bytes)", partitionPack.getKagSize() });
        addRow(new Object[] { "Essence Labels", null });
        for (UL ul : partitionPack.getEssenceContainers()) {
            String name = (ul.getName() != null) ? ul.getName() : "<unknown label>";
            addRow(new Object[] { "", name });
        }
    }

    public String getPartitionType(PartitionPackKind kind) {
        if (kind == null) {
            return "<unknown>";
        }
        return kind.name();
    }

    public String getPartitionStatus(int status) {
        if (status == 0x01) {
            return "Open, Incomplete";
        } else if (status == 0x02) {
            return "Closed, Incomplete";
        } else if (status == 0x03) {
            return "Open, Complete";
        } else if (status == 0x04) {
            return "Closed, Complete";
        } else {
            return "<unknown>";
        }
    }
}
