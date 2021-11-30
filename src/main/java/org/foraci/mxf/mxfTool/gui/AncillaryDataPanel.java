package org.foraci.mxf.mxfTool.gui;

import org.foraci.anc.afd.AfdPacket;
import org.foraci.anc.anc.*;
import org.foraci.anc.atc.AncillaryTimecodePacket;
import org.foraci.anc.cdp.CdpHeader;
import org.foraci.anc.cdp.CdpPacket;
import org.foraci.mxf.mxfTool.dataMgrs.CustomTableModel;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A panel to details about any ancillary data found, if any
 */
public class AncillaryDataPanel extends JPanel implements MxfViewListener, AncPacketListener {
    private static final Logger log = LoggerFactory.getLogger(AncillaryDataPanel.class.getName());
    private static final int DEFAULT_MAX_FRAMES_TO_INSPECT = 30;
    private static final int MAX_PACKETS_PER_LINE = 10;

    private static int MAX_FRAMES_TO_INSPECT;

    private static Map<AncPacketId, String> packetNameMap;

    static {
        packetNameMap = new HashMap<AncPacketId, String>();
        packetNameMap.put(new AncPacketId(0x61, 0x01), "EIA-708/Caption Data Packet (S334-1)");
        packetNameMap.put(new AncPacketId(0x60, 0x60), "Ancillary timecode (S12M-2)");
        packetNameMap.put(new AncPacketId(0x41, 0x01), "Payload Identification (S352M)");
        packetNameMap.put(new AncPacketId(0x41, 0x05), "Active Format Descriptor (S2016-3)");
        try {
            MAX_FRAMES_TO_INSPECT = Integer.parseInt(System.getProperty(
                    "anc.max_frames_to_inspect", Integer.toString(DEFAULT_MAX_FRAMES_TO_INSPECT)));
        } catch (NumberFormatException e) {
            log.warn("anc.max_frames_to_inspect has invalid value of {} defaulting to {}",
                    System.getProperty("anc.max_frames_to_inspect"), DEFAULT_MAX_FRAMES_TO_INSPECT);
            MAX_FRAMES_TO_INSPECT = DEFAULT_MAX_FRAMES_TO_INSPECT;
        }
    }

    private CustomTableModel model;
    private JTable table;
    private boolean done;
    private int lastLine, framesInspected, packetsInspected;

    public AncillaryDataPanel() {
        super(new BorderLayout());
        createUI();
    }

    private void createUI() {
        model = new CustomTableModel(new String[] { "DID/SDID", "Name", "Value", "Line", "Field #", "HANC?", "Chroma?" });
        table = new JTable(model);
        CustomTableCellRenderer r = new CustomTableCellRenderer();
        table.setDefaultRenderer(Object.class, r);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(125);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setMaxWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setMaxWidth(400);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setMaxWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setMaxWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setMaxWidth(60);
        table.getColumnModel().getColumn(6).setPreferredWidth(60);
        table.getColumnModel().getColumn(6).setMaxWidth(60);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void assetStartLoad() {
        model.setRowCount(0);
        done = false;
        lastLine = -1;
        framesInspected = 0;
        packetsInspected = 0;
    }

    public void assetLoaded() {
        //model.fireTableDataChanged();
    }

    public void scrollToLatest() {
        table.scrollRectToVisible(new Rectangle(0, table.getHeight(), 1, 1));
    }

    public void packet(AncPacketHeader header, AncPacketUserData payload, TrackAttributes trackAttributes) {
        if (done || framesInspected > MAX_FRAMES_TO_INSPECT) {
            done = true;
            return;
        }
        String type = String.format("0x%02x/0x%02x", header.getId().getDid(), header.getId().getSdid());
        String name = packetNameMap.get(header.getId());
        if (name == null) {
            name = "???";
        }
        String value;
        if (payload instanceof AncillaryTimecodePacket) {
            value = ((AncillaryTimecodePacket)payload).getTimecode();
        } else if (payload instanceof CdpPacket) {
            CdpPacket cdpPacket = (CdpPacket) payload;
            CdpHeader cdpHeader = cdpPacket.getCdpHeader();
            value = String.format("rate:%s, TC:%b, CC:%b, Srv Info:%b, Active:%b",
                    cdpRateToString(cdpHeader.getFrameRate()), cdpHeader.isTcPresent(),
                    cdpHeader.isCcPresent(), cdpHeader.isServiceInfoPresent(),
                    cdpHeader.isCaptionServiceActive());
        } else if (payload instanceof AfdPacket) {
            AfdPacket afdPacket = (AfdPacket) payload;
            value = afdPacket.getDescription();
        } else {
            value = "N/A";
        }
        if (trackAttributes != null) {
            if (lastLine == -1 || trackAttributes.getLine() < lastLine) {
                if (framesInspected < MAX_FRAMES_TO_INSPECT) {
                    model.addRow(new Object[] { "Frame " + (framesInspected + 1), null, null, null, null, null, null });
                } else if (framesInspected == MAX_FRAMES_TO_INSPECT) {
                    done = true;
                    return;
                }
                nextFrame();
            } else if (lastLine == trackAttributes.getLine()) {
                ++packetsInspected;
                if (packetsInspected == MAX_PACKETS_PER_LINE) {
                    model.addRow(new Object[] { "...", null, null, null, null, null, null });
                    done = true;
                    return;
                }
            }
            model.addRow(new Object[] { type, name, value, trackAttributes.getLine(),
                    trackAttributes.getField(), trackAttributes.isInHanc(),
                    trackAttributes.isChroma() });
            lastLine = trackAttributes.getLine();
        } else {
            model.addRow(new Object[] { type, name, value, "?", "?", "?", "?" });
            nextFrame();
        }
    }

    private void nextFrame() {
        ++framesInspected;
        packetsInspected = 0;
    }

    private String cdpRateToString(int rate) {
        if (rate > 0x8) {
            return "RESERVED!";
        }
        switch(rate) {
            case 0x1:
                return "24000/1001";
            case 0x2:
                return "24";
            case 0x3:
                return "25";
            case 0x4:
                return "30000/1001";
            case 0x5:
                return "30";
            case 0x6:
                return "50";
            case 0x7:
                return "60000/1001";
            case 0x8:
                return "60";
            default:
                return "FORBIDDEN!";
        }
    }

    public boolean done() {
        return done;
    }
}
