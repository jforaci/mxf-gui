package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfReader.PartitionPack;
import org.foraci.mxf.mxfReader.RandomIndexPack;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfTool.dataMgrs.AnalyzeEssenceContainerController;
import org.foraci.mxf.mxfTool.dataMgrs.EssenceTrack;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Represents the interface into the UI for an MXF asset
 */
public interface MxfView {
    GroupNode getRootGroupNode();
    Set<GroupNode> getGroups();
    List<PartitionPack> getPartitionPackList();
    RandomIndexPack getRandomIndexPack();
    void setRandomIndexPack(RandomIndexPack randomIndexPack);
    File getFile();
    List<File> getExternalFileList();
    List<EssenceTrack> getExportableTrackList();

    CaptionServicePanel getCaptionServicePanel(int serviceNumber);
    AncillaryDataPanel getAncPacketListener();
    DolbyETrackInfoPanel getDolbyEInfoPanel(EssenceTrack track);
    DefaultMutableTreeNode getStructureTreeRoot();
    AnalyzeEssenceContainerController getEssenceController();
    boolean isDebug();
    void debug(String message);
    void info(String message);
    void warn(String message);
    void error(String message, Exception e);
    void dump(String line);
}
