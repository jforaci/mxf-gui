package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.LeafNode;
import org.foraci.mxf.mxfReader.entities.Rational;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfReader.registries.Labels;
import org.foraci.mxf.mxfReader.registries.Metadata;
import org.foraci.anc.util.timecode.Timecode;
import org.foraci.anc.util.timecode.TimecodeBase;
import org.foraci.mxf.mxfTool.dataMgrs.Utils;
import org.foraci.mxf.mxfTool.gui.paint.StripePaint;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Iterator;

/**
 * Simple graphic panel to visualize track layout
 */
public class TrackVisPanel extends JPanel implements Scrollable {
    private MxfView view;
    private int preferredHeight = 500;

    public TrackVisPanel(MxfView view) {
        this.view = view;
        setBackground(Color.WHITE);
    }

    private void drawLegend(Graphics2D g, Rectangle r) {
        g.setColor(Color.WHITE);
        g.fillRect(r.x, r.y, r.x + r.width, r.y + r.height);
        g.setColor(Color.BLACK);
        g.drawRect(r.x, r.y, r.x + r.width, r.y + r.height);
        int numBoxes = 4;
        int boxHeight = r.height / numBoxes;
        r.x += 5;
        r.y += 5;
        g.drawString("Picture", r.x + 25, r.y + 15);
        g.setColor(Color.GREEN);
        g.fillRect(r.x, r.y, 20, 20);
        g.setColor(Color.BLACK);
        g.drawRect(r.x, r.y, 20, 20);
        r.y += boxHeight;
        g.drawString("Sound", r.x + 25, r.y + 15);
        g.setColor(Color.BLUE);
        g.fillRect(r.x, r.y, 20, 20);
        g.setColor(Color.BLACK);
        g.drawRect(r.x, r.y, 20, 20);
        r.y += boxHeight;
        g.drawString("Data", r.x + 25, r.y + 15);
        g.setColor(Color.RED);
        g.fillRect(r.x, r.y, 20, 20);
        g.setColor(Color.BLACK);
        g.drawRect(r.x, r.y, 20, 20);
        r.y += boxHeight;
        g.drawString("Timecode", r.x + 25, r.y + 15);
        g.setPaint(new StripePaint(Color.LIGHT_GRAY, 4));
        g.fillRect(r.x, r.y, 20, 20);
        g.setColor(Color.BLACK);
        g.drawRect(r.x, r.y, 20, 20);
    }

    private void drawClips(Graphics2D g, Rectangle r, GroupNode seq, GroupNode track, int clipSize) {
        LeafNode clipsNode = (LeafNode) seq.find(Metadata.ComponentsinSequence);
        if (clipsNode == null) {
            return;
        }
        final int numClips = clipsNode.refs().size();
        final int clipWidth = r.width / numClips;
        for (Iterator<GroupNode> i = clipsNode.refs().iterator(); i.hasNext();) {
            GroupNode clip = i.next();
            LeafNode dataDefinitionNode = (LeafNode) clip.find(Metadata.ComponentDataDefinition);
            UL dataDefinitionLabel = (UL) dataDefinitionNode.values().get(0);
            if (Labels.PictureEssenceTrack.equals(dataDefinitionLabel)) {
                g.setColor(Color.GREEN.darker());
                g.fillRect(r.x + 2, r.y, clipWidth - 4, clipSize);
                g.setColor(Color.WHITE);
            } else if (Labels.SoundEssenceTrack.equals(dataDefinitionLabel)) {
                g.setColor(Color.BLUE.darker());
                g.fillRect(r.x + 2, r.y, clipWidth - 4, clipSize);
                g.setColor(Color.WHITE);
            } else if (Labels.DataEssenceTrack.equals(dataDefinitionLabel)) {
                g.setColor(Color.RED.darker());
                g.fillRect(r.x + 2, r.y, clipWidth - 4, clipSize);
                g.setColor(Color.WHITE);
            } else if (Labels.SMPTE12MTimecodeTrackInactiveUserBits.equals(dataDefinitionLabel)
                    || Labels.SMPTE12MTimecodeTrackActiveUserBits.equals(dataDefinitionLabel)) {
                final Color lightGray = new Color(220, 220, 220);
                g.setPaint(new StripePaint(lightGray, 4));
                g.fillRect(r.x + 2, r.y, clipWidth - 4, clipSize);
                g.setColor(Color.BLACK);
            } else {
                g.setColor(Color.WHITE);
                g.fillRect(r.x + 2, r.y, clipWidth - 4, clipSize);
                g.setColor(Color.BLACK);
            }
            Rational editRate = (Rational) track.value(Metadata.TimelineEditRate);
            Number origin = (Number) track.value(Metadata.Origin);
            Number frameDuration = (Number) clip.value(Metadata.ComponentLength); // duration in edit units
            String duration;
            if (Groups.TimecodeComponent.equals(clip.ul())) {
                int startFrames = ((Number)clip.value(Metadata.StartTimecode)).intValue();
                boolean dropFrameTc = (((Number)clip.value(Metadata.DropFrame)).intValue() == 1);
                String start;
                String drop = "";
                if (dropFrameTc) {
                    drop = " (drop)";
                    duration = Timecode.fromEditUnits(TimecodeBase.NTSC, frameDuration.intValue()).toString();
                    start = Timecode.fromEditUnits(TimecodeBase.NTSC, startFrames * 2).toString();
                } else {
                    int roundedTimecodeBase = ((Number)clip.value(Metadata.RoundedTimecodeTimebase)).intValue();
                    duration = Utils.getTimecodeDuration(frameDuration.longValue(), roundedTimecodeBase);
                    start = Utils.getTimecodeDuration(startFrames, roundedTimecodeBase);
                }
                g.drawString(String.format("Start: %s%s, Dur: %s, %d units", start, drop, duration, frameDuration.intValue()), r.x + 2 + 2, r.y + clipSize - 2);
            } else {
                float realtimeDuration = (float) (frameDuration.intValue() * editRate.getDenominator()) / editRate.getNumerator();
                duration = Utils.getRealTimeDuration(realtimeDuration) + ", " + frameDuration + " units";
                String start = clip.string(Metadata.StartTimeRelativetoReference1);
                g.drawString("Origin: " + origin + ", Rate: " + editRate, r.x + 2 + 2, r.y + 10);
                g.drawString("Start: " + start + ", Dur: " + duration, r.x + 2 + 2, r.y + clipSize - 2);
            }
            r.x += clipWidth;
        }
    }

    private void drawTracks(Graphics2D g, Rectangle r, LeafNode tracksNode, int trackSize) {
        int x = r.x;
        int y = r.y;
        int c = 0;
        g.setFont(g.getFont().deriveFont(Font.BOLD));
        for (Iterator<GroupNode> t = tracksNode.refs().iterator(); t.hasNext();) {
            GroupNode track = t.next();
            if (!Groups.TimelineTrack.equals(track.ul())) {
                continue;
            }
//            String trackName = Utils.getTrackLabel(track);
            GroupNode seq = track.ref(Metadata.Segment);
            g.setColor(Color.RED);
//            g.drawRect(x, y, r.width, trackSize);
            AffineTransform tx = g.getTransform();
            g.rotate(-Math.PI / 2, x + 10, y + trackSize - 2);
            c++;
            g.drawString("T #" + c, x + 10, y + trackSize - 2);
            g.setTransform(tx);
            Rectangle rs = new Rectangle(x + 10 + 2, y + 2, r.width - 10 - 4, trackSize - 4);
            drawClips(g, rs, seq, track, trackSize - 4);
            y += trackSize;
        }
    }

    private void drawPackage(Graphics2D g2, Rectangle r, GroupNode pkg, String packageName) {
        Graphics2D g = (Graphics2D) g2.create();
        final int trackSize = 30;
        LeafNode tracksNode = (LeafNode) pkg.find(Metadata.Tracks);
        int numTracks = tracksNode.refs().size();
        g.setColor(Color.BLUE);
        g.drawRect(r.x, r.y, r.width, r.height); //numTracks * trackSize + 5
        g.drawString(packageName, r.x + 2, r.y + 10 + 2);
        Rectangle rs = new Rectangle(r.x + 5, r.y + 5 + 10, r.width - 10, r.height - 10); //numTracks * trackSize
        g.clip(new Rectangle(rs.x, rs.y, r.width, r.height - 5 - 10));
        drawTracks((Graphics2D) g, rs, tracksNode, trackSize);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, preferredHeight);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics gg = g.create();
        super.paintComponent(g);
        if (view.getRootGroupNode() == null) {
            return;
        }
        drawPanel(gg);
    }

    void reset() {
        // reset the preferred height and revalidate
        preferredHeight = 100;
        revalidate();
    }

    private int getPackageSize(GroupNode pkg) {
        final int maxPackageSize = 1000;
        final int trackSize = 30;
        LeafNode tracksNode = (LeafNode) pkg.find(Metadata.Tracks);
        int numTracks = tracksNode.refs().size();
        return Math.min(maxPackageSize, numTracks * trackSize + 20);
    }

    private void drawPanel(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
//        g.drawRect(50, 50, getWidth() - 100, getHeight() - 100);
//        if (true) return;
        final int packageSize = 200;
        int x = 5, y = 0, w = getWidth() - 10, h = 0;
        for (Iterator<GroupNode> i = view.getGroups().iterator(); i.hasNext();) {
            GroupNode group = i.next();
            if (Groups.MaterialPackage.equals(group.ul())) {
                h = getPackageSize(group);
                Rectangle r = new Rectangle(x, y, w, h);
                drawPackage((Graphics2D) g, r, group, "Material Package");
                y += h + 10;
            }
        }
        int p = 0;
        for (Iterator<GroupNode> i = view.getGroups().iterator(); i.hasNext();) {
            GroupNode group = i.next();
            if (Groups.SourcePackage.equals(group.ul())) {
                h = getPackageSize(group);
                Rectangle r = new Rectangle(x, y, w, h);
                p++;
                drawPackage((Graphics2D) g, r, group, "Source Package " + p);
                y += h + 10;
            }
        }
        preferredHeight = y;
        // draw the legend
        Rectangle r = new Rectangle(getWidth() - 100, 0, 100, 120);
        drawLegend((Graphics2D) g, r);
        revalidate();
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 100;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 100;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
