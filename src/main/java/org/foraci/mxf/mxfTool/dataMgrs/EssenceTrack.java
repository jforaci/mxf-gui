package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.mxf.mxfReader.entities.GroupNode;

import java.io.File;

/**
 * Represents a track of essence
 */
public class EssenceTrack
{
    private File file;
    private long bodySid;
    private long trackNumber;
    private GroupNode filePackage;
    private GroupNode track;

    public EssenceTrack(File file, long bodySid, long trackNumber, GroupNode filePackage, GroupNode track) {
        this.file = file;
        this.bodySid = bodySid;
        this.trackNumber = trackNumber;
        this.filePackage = filePackage;
        this.track = track;
    }

    public File getFile() {
        return file;
    }

    public long getBodySid() {
        return bodySid;
    }

    public long getTrackNumber() {
        return trackNumber;
    }

    public GroupNode getFilePackage() {
        return filePackage;
    }

    public GroupNode getTrack() {
        return track;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EssenceTrack that = (EssenceTrack) o;

        if (bodySid != that.bodySid) return false;
        if (trackNumber != that.trackNumber) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (bodySid ^ (bodySid >>> 32));
        result = 31 * result + (int) (trackNumber ^ (trackNumber >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return file.getPath() + ",B=" + getBodySid() + ",T=" + getTrackNumber();
    }
}
