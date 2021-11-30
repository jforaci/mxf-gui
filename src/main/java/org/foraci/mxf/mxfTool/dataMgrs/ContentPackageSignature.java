package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.mxf.mxfReader.EssenceContainerOutputController;
import org.foraci.mxf.mxfReader.UL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the signature, or makeup, of a content package of essence (see S379M)
 */
public class ContentPackageSignature
{
    private static final byte[] SDTI_CP_SYS_METADATA_KEY = {0x06,0x0E,0x2B,0x34,0x02,0x05,0x01,0x01,0x0d,0x01,0x03,0x01,0x04,0x01,0x01,0x00,};
    private static final byte[] SDTI_CP_PACKAGE_METADATA_KEY = {0x06,0x0E,0x2B,0x34,0x02,0x43,0x01,0x01,0x0d,0x01,0x03,0x01,0x04,0x01,0x02,0x00,};
    private static final byte[] SYSTEM_SCHEME_1_KEY = {0x06,0x0E,0x2B,0x34,0x02,0x53,0x01,0x01,0x0d,0x01,0x03,0x01,0x14,0x02,0x01,0x00,};

    private List<UL> keys;
    private int occurrences = 0;

    ContentPackageSignature() {
        this.keys = new ArrayList<UL>();
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    void addElementKey(UL key) {
        if (key == null) {
            throw new NullPointerException("key can not be null");
        }
        keys.add(key);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<UL> i = keys.iterator(); i.hasNext();) {
            UL key = i.next();
            if (key.isGcSystemElement()) {
                if (key.isFirstGcSystemElementInCp()) {
                    if (Arrays.equals(key.getKey(), SDTI_CP_SYS_METADATA_KEY)) {
                        sb.append("[SDTI-CP Sys]");
                    } else if (Arrays.equals(key.getKey(), SYSTEM_SCHEME_1_KEY)) {
                        sb.append("[GC Sys scheme 1]");
                    } else {
                        sb.append("[SYS1]");
                    }
                } else {
                    if (Arrays.equals(key.getKey(), SDTI_CP_PACKAGE_METADATA_KEY)) {
                        sb.append("[SDTI-CP Pkg]");
                    } else {
                        sb.append("[SYS]");
                    }
                }
            } else if (key.isDataEssence()) {
                long trackNumber = key.getTrackNumber();
                if (trackNumber == EssenceContainerOutputController.SMPTE436_VBI_TRACK) {
                    sb.append("[Data (VBI)]");
                } else if (trackNumber == EssenceContainerOutputController.SMPTE436_ANC_TRACK) {
                    sb.append("[Data (Anc)]");
                } else {
                    sb.append("[Data]");
                }
            } else if (key.isSoundEssence()) {
                sb.append("[Sound]");
            } else if (key.isPictureEssence()) {
                sb.append("[Picture]");
            } else if (key.isCompoundEssence()) {
                sb.append("[Compound]");
            } else {
                sb.append("[?]");
            }
            if (i.hasNext()) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContentPackageSignature that = (ContentPackageSignature) o;

        if (!keys.equals(that.keys)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return keys.hashCode();
    }
}
