package org.foraci.mxf.mxfTool.dataMgrs;

import org.foraci.mxf.mxfReader.EssenceContainerOutputController;
import org.foraci.mxf.mxfReader.UL;
import org.foraci.mxf.mxfTool.gui.MxfView;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Essence output controller
 */
public class AnalyzeEssenceContainerController implements EssenceContainerOutputController {
    private final File baseName;
    private final MxfView view;
    private List<ContentPackageSignature> signatures;
    private ContentPackageSignature currentPackageSignature;
    private static final int CP_ELEMENT_LIMIT = 100;
    private long firstTrackNumber = 0;
    private int contentPackagesFound = 0;

    public AnalyzeEssenceContainerController(File baseName, MxfView view) {
        this.baseName = baseName;
        this.view = view;
        this.signatures = new ArrayList<ContentPackageSignature>();
    }

    public boolean needEssence() {
        return (contentPackagesFound < CP_ELEMENT_LIMIT);
    }

    public OutputStream getOutputForBodySidAndTrack(long bodySid, UL ul) throws IOException {
        if (contentPackagesFound == CP_ELEMENT_LIMIT) {
            return null;
        }
        long trackNumber = ul.getTrackNumber();
        // TODO: technically, we should use body SID and track number here
        if (firstTrackNumber == 0) {
            startNewContentPackage();
            firstTrackNumber = trackNumber;
        } else if (firstTrackNumber == trackNumber) {
            contentPackagesFound++;
            if (contentPackagesFound == CP_ELEMENT_LIMIT) {
                return null;
            }
            startNewContentPackage();
        }
        currentPackageSignature.addElementKey(ul);
        return null;
    }

    public OutputStream getOutputForSystemElement(long bodySid, UL ul) throws IOException {
        if (contentPackagesFound == CP_ELEMENT_LIMIT) {
            return null;
        }
        byte[] key = ul.getKey();
        long trackNumber = -(((key[12] & 0xFF) << 24) | ((key[13] & 0xFF) << 16) | ((key[14] & 0xFF) << 8) | (key[15] & 0xFF));
        boolean isNewPackage = true;
        if (firstTrackNumber == 0) {
            startNewContentPackage();
            firstTrackNumber = trackNumber;
        } else if (firstTrackNumber == trackNumber) {
            contentPackagesFound++;
            if (contentPackagesFound == CP_ELEMENT_LIMIT) {
                return null;
            }
            startNewContentPackage();
        } else {
            isNewPackage = false;
        }
        currentPackageSignature.addElementKey(ul);
        if (!ul.isFirstGcSystemElementInCp() && isNewPackage) {
            view.warn("missing start (type=0x01) system item");
        }
        return null;
    }

    private void startNewContentPackage() {
        if (currentPackageSignature != null) {
            signatures.add(currentPackageSignature);
        }
        currentPackageSignature = new ContentPackageSignature();
    }

    public List<ContentPackageSignature> getSignatures() {
        return signatures;
    }

    public void close() throws IOException {
        if (currentPackageSignature != null) {
            signatures.add(currentPackageSignature);
            currentPackageSignature = null;
        }
        // count occurrences of the same signature to make data more concise
        ContentPackageSignature lastCpSignature = null;
        int count = 0;
        for (Iterator<ContentPackageSignature> i = signatures.iterator(); i.hasNext();) {
            ContentPackageSignature cpSignature = i.next();
            if (!cpSignature.equals(lastCpSignature)) {
                if (lastCpSignature != null) {
                    lastCpSignature.setOccurrences(count);
                }
                count = 1;
                lastCpSignature = cpSignature;
                continue;
            }
            i.remove();
            ++count;
            if (!i.hasNext()) {
                lastCpSignature.setOccurrences(count);
            }
        }
    }
}
