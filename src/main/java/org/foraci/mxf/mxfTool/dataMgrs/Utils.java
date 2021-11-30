package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfReader.registries.Metadata;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A gathering place for lost code
 */
public class Utils {
    public static int selectFirstMaterialStartTime(Set<GroupNode> groups) {
        for (Iterator<GroupNode> i = groups.iterator(); i.hasNext();) {
            GroupNode group = i.next();
            if (!Groups.MaterialPackage.equals(group.ul())) {
                continue;
            }
            for (Iterator<GroupNode> t = group.refs(Metadata.Tracks).iterator(); t.hasNext();) {
                GroupNode track = t.next();
                if (!Groups.TimelineTrack.equals(track.ul())) {
                    continue;
                }
                List<GroupNode> clips = track.ref(Metadata.Segment)
                        .refs(Metadata.ComponentsinSequence);
                for (Iterator<GroupNode> c = clips.iterator(); c.hasNext();) {
                    GroupNode clip = c.next();
                    if (!Groups.TimecodeComponent.equals(clip.ul())) {
                        continue;
                    }
                    return ((Number)clip.value(Metadata.StartTimecode)).intValue();
                }
            }
        }
        return 0;
    }

    public static String getTrackLabel(GroupNode track) {
        String label;
        label = track.string(Metadata.TrackName);
        if (label == null) {
            label = track.string(Metadata.TrackName1);
        }
        if (label == null) {
            GroupNode sequence = track.ref(Metadata.Segment);
            if (sequence != null) {
                UL ul = (UL) sequence.value(Metadata.ComponentDataDefinition);
                label = ul.getName();
            }
        }
        if (label == null) {
            label = "Track ID " + track.string(Metadata.TrackID);
        }
        return label;
    }

    public static String getRealTimeDuration(float fseconds) {
        int seconds = (int) fseconds;
        float fractionOfSecond = fseconds - seconds;
        int minutes = (seconds / 60) % 60;
        int hours = seconds / 3600;
        seconds = (seconds % 60) % 60;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, (int) (fractionOfSecond * 1000));
    }

    public static String getTimecodeDuration(long editUnits, final int roundedTimecodeBase) {
        long hours = editUnits / (3600 * roundedTimecodeBase);
        long minutes = (editUnits / (60 * roundedTimecodeBase)) % 60;
        long seconds = (editUnits / roundedTimecodeBase) % 60;
        long fractionalSecondInEditUnits = editUnits % roundedTimecodeBase;
        return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, fractionalSecondInEditUnits);
    }
}
