package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfReader.*;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.LeafNode;
import org.foraci.mxf.mxfReader.exceptions.ValidationException;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfReader.registries.Metadata;
import org.foraci.mxf.mxfTool.dataMgrs.AnalyzeEssenceContainerController;
import org.foraci.mxf.mxfTool.dataMgrs.EssenceTrack;
import org.foraci.mxf.mxfTool.gui.updaters.ShowLoggerErrorRunnable;
import org.foraci.mxf.mxfTool.mxf.FriendlyUnknownValueParserFactory;
import org.foraci.mxf.mxfTool.dnd.FileDropTransferHandler;
import org.foraci.mxf.mxfTool.mxf.MxfAncReader;
import org.foraci.mxf.mxfTool.mxf.MxfAssetReader;
import org.foraci.mxf.mxfTool.mxf.MxfExportReader;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * MXF inspection tool
 *
 * @author jforaci
 */
public class MxfTool implements ActionListener, MxfView
{
    private static final Logger log = LoggerFactory.getLogger(MxfTool.class.getName());

    private final JFrame frame;
    private final Canvas canvas;
    private JMenuItem loadMenuItem, exportEssenceMenuItem;

    private boolean debug = false;
    private String path;
    private JToggleButton playButton;
    private JSlider scanBar;
    private boolean adjusting = false;
    private JMenu fileMenu, exportMenu;
    private LoadOptionsPanel loadOptionsPanel;
    private StructurePanel structurePanel;
    private JTabbedPane overviewPanel;
    private TrackVisualizationPanel trackVisualizationPanel;
    private InfoPanel infoPanel;
    private PartitionPanel partitionPanel;
    private ConsolePanel dumpConsolePanel;
    private MetadataTreePanel metadataTreePanel;
    private LoggerPanel loggerPanel;
    private AncillaryDataPanel ancillaryDataPanel;
    private CaptionPanel ccPanel;
    private DolbyEPanel dolbyEPanel;

    private DefaultMutableTreeNode structTreeRoot;
    private List<PartitionPack> partitionPackList;
    private RandomIndexPack randomIndexPack;
    private List<File> externalFilesList;
    private List<EssenceTrack> exportableTrackList;
    private MxfAssetReader reader;
    private AnalyzeEssenceContainerController essenceController;

    public static void main(String[] args)
    {
        String debugSetting = System.getProperty("debug", "false");
        new MxfTool("true".equalsIgnoreCase(debugSetting));
    }

    public MxfTool(boolean debug)
    {
        log.trace("starting");
        this.debug = debug;
        UL.setParserFactory(new FriendlyUnknownValueParserFactory());
//        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            error("Could not set system LnF", e);
        }
        structTreeRoot = new DefaultMutableTreeNode("root");
        partitionPackList = new ArrayList<PartitionPack>();
        randomIndexPack = null;
        externalFilesList = new ArrayList<File>();
        exportableTrackList = new ArrayList<EssenceTrack>();
        canvas = createVideoSurface();
        JPanel previewPanel = createPreviewPanel(canvas);
        JPanel mainPanel = createMainPanel(previewPanel);
        overviewPanel = createOverviewPanel();
        JMenuBar menuBar = createMenus();

        frame = new JFrame("MXF GUI");
        FileDropTransferHandler transferHandler = new FileDropTransferHandler(this);
        frame.getRootPane().setTransferHandler(transferHandler);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, mainPanel, overviewPanel);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
//        frame.getContentPane().add(overviewPanel, BorderLayout.SOUTH);
//        frame.add(previewPanel, BorderLayout.SOUTH);
        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.addWindowListener(new WindowAdapter()
//        {
//            @Override
//            public void windowClosing(WindowEvent e)
//            {
//                debug("closing");
////                mediaPlayer.release();
////                mediaPlayerFactory.release();
//            }
//        });

        frame.setPreferredSize(new Dimension(1000, 900));
        frame.pack();
        splitPane.setDividerLocation(0.5);
        frame.setIconImages(Arrays.asList(
                new ImageIcon(MxfTool.class.getResource(
                        "/org/foraci/mxf/mxfTool/res/icon-16x16.png")).getImage(),
                new ImageIcon(MxfTool.class.getResource(
                        "/org/foraci/mxf/mxfTool/res/icon-32x32.png")).getImage(),
                new ImageIcon(MxfTool.class.getResource(
                        "/org/foraci/mxf/mxfTool/res/icon-48x48.png")).getImage(),
                new ImageIcon(MxfTool.class.getResource(
                        "/org/foraci/mxf/mxfTool/res/icon-64x64.png")).getImage()
                ));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenus() {
        JMenuBar menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        loadMenuItem = new JMenuItem("Load...");
        loadMenuItem.addActionListener(this);
        fileMenu.add(loadMenuItem);
        menuBar.add(fileMenu);
        exportMenu = new JMenu("Export");
        exportMenu.setEnabled(false);
        exportEssenceMenuItem = new JMenuItem("Essence to files...");
        exportEssenceMenuItem.addActionListener(this);
        exportMenu.add(exportEssenceMenuItem);
        menuBar.add(exportMenu);
        return menuBar;
    }

    private JTabbedPane createOverviewPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        loggerPanel = createLoggerPanel();
        tabbedPane.add("Messages", loggerPanel);
        infoPanel = createInfoPanel();
        tabbedPane.add("General", infoPanel);
        partitionPanel = createPartitionPanel();
        tabbedPane.add("Partitions", partitionPanel);
        metadataTreePanel = createMetadataTreePanel();
        tabbedPane.add("Metadata", metadataTreePanel);
        trackVisualizationPanel = createTrackVisualizationPanel();
        tabbedPane.add("Track Layout", trackVisualizationPanel);
        dumpConsolePanel = createDumpConsolePanel();
        tabbedPane.add("Dump", dumpConsolePanel);
        ancillaryDataPanel = createAncillaryDataPanel();
        tabbedPane.add("Anc/VBI", ancillaryDataPanel);
        ccPanel = createCaptionPanel();
        tabbedPane.add("CC (708)", ccPanel);
        dolbyEPanel = createDolbyEPanel();
        tabbedPane.add("Dolby E", dolbyEPanel);
        tabbedPane.setSelectedIndex(1);
        return tabbedPane;
    }

    private MetadataTreePanel createMetadataTreePanel() {
        MetadataTreePanel panel = new MetadataTreePanel(this);
        return panel;
    }

    private CaptionPanel createCaptionPanel() {
        CaptionPanel panel = new CaptionPanel();
        return panel;
    }

    private AncillaryDataPanel createAncillaryDataPanel() {
        AncillaryDataPanel panel = new AncillaryDataPanel();
        return panel;
    }

    private DolbyEPanel createDolbyEPanel() {
        DolbyEPanel panel = new DolbyEPanel();
        return panel;
    }

    private CaptionServicePanel createCaptionsSinkPanelForService(int serviceNumber) {
        CaptionServicePanel panel = new CaptionServicePanel();
        return panel;
    }

    private LoggerPanel createLoggerPanel() {
        LoggerPanel panel = new LoggerPanel();
        return panel;
    }

    private TrackVisualizationPanel createTrackVisualizationPanel() {
        TrackVisualizationPanel panel = new TrackVisualizationPanel(this);
        return panel;
    }

    private InfoPanel createInfoPanel() {
        InfoPanel panel = new InfoPanel(this);
        return panel;
    }

    private PartitionPanel createPartitionPanel() {
        PartitionPanel panel = new PartitionPanel(this);
        return panel;
    }

    private ConsolePanel createDumpConsolePanel() {
        ConsolePanel panel = new ConsolePanel(this);
        return panel;
    }

    private JPanel createMainPanel(Component preview) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.gridx = gbc.gridy = 0;
        JPanel panel = new JPanel(new GridBagLayout());
        gbc.weighty = 0;
        loadOptionsPanel = new LoadOptionsPanel();
        panel.add(loadOptionsPanel, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        panel.add(preview, gbc);
        gbc.gridy = 0;
        gbc.gridx++;
        gbc.gridheight = 2;
        gbc.weightx = gbc.weighty = 3;
        structurePanel = createStructurePanel();
        panel.add(structurePanel, gbc);
        return panel;
    }

    private StructurePanel createStructurePanel() {
        StructurePanel panel = new StructurePanel(this);
        return panel;
    }

    private Canvas createVideoSurface()
    {
        Canvas canvas = new Canvas();
        canvas.setBackground(Color.black);
//        canvas.setSize(800, 600); // Only for initial layout
        canvas.setMinimumSize(new Dimension(200, 200));
//        canvas.setMaximumSize(new Dimension(200, 200));
        canvas.setPreferredSize(new Dimension(200, 200));
        return canvas;
    }

    private JPanel createPreviewPanel(Canvas canvas)
    {
        JPanel controlPanel = createControlPanel();
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.add(controlPanel, BorderLayout.SOUTH);
        previewPanel.add(canvas, BorderLayout.CENTER);
        return previewPanel;
    }

    private JPanel createControlPanel()
    {
        JPanel controlPanel = new JPanel(null);
        BoxLayout boxLayout = new BoxLayout(controlPanel, BoxLayout.X_AXIS);
        controlPanel.setLayout(boxLayout);
        playButton = new JToggleButton("Play");
        playButton.addActionListener(this);
        playButton.setEnabled(false);
        controlPanel.add(playButton);
        scanBar = new JSlider(JSlider.HORIZONTAL);
        scanBar.setMinimum(0);
        scanBar.setMaximum(0);
        scanBar.setMinorTickSpacing(60);
        scanBar.setMajorTickSpacing(5 * 60);
        scanBar.setPaintTicks(true);
//        scanBar.addChangeListener(new ChangeListener()
//        {
//            public void stateChanged(ChangeEvent e)
//            {
//                if (adjusting || scanBar.getValueIsAdjusting() /* || !mediaPlayer.isPlayable() || !mediaPlayer.isSeekable() */ ) {
//                    return;
//                }
//                debug("skip to: " + scanBar.getValue() * 1000);
////                mediaPlayer.setTime(scanBar.getValue() * 1000);
//            }
//        });
        controlPanel.add(scanBar);
        return controlPanel;
    }

    public GroupNode getRootGroupNode() {
        if (reader == null) {
            return null;
        }
        return reader.getRootGroupNode();
    }

    public Set<GroupNode> getGroups() {
        if (reader == null) {
            return null;
        }
        return reader.getGroups();
    }

    public AncillaryDataPanel getAncPacketListener() {
        return ancillaryDataPanel;
    }

    public CaptionServicePanel getCaptionServicePanel(int serviceNumber) {
        return ccPanel.getServicePanel(serviceNumber);
    }

    public DolbyETrackInfoPanel getDolbyEInfoPanel(EssenceTrack track) {
        return dolbyEPanel.getTrackPanel(track);
    }

    public DefaultMutableTreeNode getStructureTreeRoot() {
        return structTreeRoot;
    }

    public List<PartitionPack> getPartitionPackList() {
        return partitionPackList;
    }

    public RandomIndexPack getRandomIndexPack() {
        return randomIndexPack;
    }

    public void setRandomIndexPack(RandomIndexPack randomIndexPack) {
        this.randomIndexPack = randomIndexPack;
    }

    public AnalyzeEssenceContainerController getEssenceController() {
        return essenceController;
    }

    public File getFile() {
        return reader.getFile();
    }

    public List<File> getExternalFileList() {
        return externalFilesList;
    }

    public List<EssenceTrack> getExportableTrackList() {
        return exportableTrackList;
    }

    public void dump(String line) {
        dumpConsolePanel.dump(line);
    }

    public void dropFile(java.util.List<File> files, Object source)
    {
        frame.setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            info("loading " + files);
            log.debug("dropped on " + source);
            if (files.isEmpty()) {
                return;
            }
            File file = files.get(0);
            try {
                try {
                    readFile(file, true);
                } catch (ValidationException e) {
                    displayValidationWarnings(e);
                    readFile(file, false);
                }
            } catch (ValidationException e) {
                displayValidationWarnings(e);
            } catch (Exception e) {
                error("Error reading " + reader.getFile(), e);
            }
            frame.setTitle(file.getAbsolutePath() + " - MXF GUI");
            exportMenu.setEnabled(true);
        } finally {
            frame.setCursor( Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void displayValidationWarnings(ValidationException e) {
        warn("Validation exception" + ((e.getMessages().size() > 1) ? "s:" : ":"));
        for (String message : e.getMessages()) {
            warn("  \u2022 " + message);
        }
        warn("  re-parsing, trying to read any partition's metadata...");
    }

    private void readFile(File file, boolean readOnlyCloseCompletePartition) throws IOException
    {
        structurePanel.assetStartLoad();
        trackVisualizationPanel.assetStartLoad();
        infoPanel.assetStartLoad();
        partitionPanel.assetStartLoad();
        dumpConsolePanel.assetStartLoad();
        metadataTreePanel.assetStartLoad();
        loggerPanel.assetStartLoad();
        ancillaryDataPanel.assetStartLoad();
        ccPanel.assetStartLoad();
        dolbyEPanel.assetStartLoad();
        essenceController = new AnalyzeEssenceContainerController(file, this);
        reader = new MxfAssetReader(file, this);
        reader.setDebugPrint(debug);
        reader.setReadClosedCompleteMetadataOnly(readOnlyCloseCompletePartition);
        reader.setMetadataReadMode((loadOptionsPanel.isReadingFromFooter())
                ? MxfReader.MetadataReadMode.FooterOnly
                : MxfReader.MetadataReadMode.All);
        reader.readAll();
        buildExportableTrackList();
        lookForAncillaryData();
        reader.validate();
        reader.dumpTree();
        structurePanel.assetLoaded();
        trackVisualizationPanel.assetLoaded();
        infoPanel.assetLoaded();
        partitionPanel.assetLoaded();
        dumpConsolePanel.assetLoaded();
        metadataTreePanel.assetLoaded();
        loggerPanel.assetLoaded();
        ancillaryDataPanel.assetLoaded();
        ccPanel.assetLoaded();
        dolbyEPanel.assetLoaded();
    }

    private void lookForAncillaryData() {
        List<File> allFiles = new ArrayList<File>();
        allFiles.add(getFile());
        allFiles.addAll(getExternalFileList());
        for (File file : allFiles) {
            List<EssenceTrack> tracksToScan = new ArrayList<EssenceTrack>();
            for (EssenceTrack essenceTrack : getExportableTrackList()) {
                if (!file.equals(essenceTrack.getFile())) {
                    continue;
                }
                if (loadOptionsPanel.isDecodingDolbyE() && isSoundEssenceDescriptor(essenceTrack)) {
                    tracksToScan.add(essenceTrack);
                } else if (/*loadOptionsPanel.isDecoding708() && */ isVancVbiEssenceDescriptor(essenceTrack)) {
                    tracksToScan.add(essenceTrack);
                }
            }
            if (tracksToScan.isEmpty()) {
                continue;
            }
            // scan file
            MxfAncReader ancReader = new MxfAncReader(
                    file, tracksToScan, this);
            ancReader.setDecodeCea708(loadOptionsPanel.isDecoding708());
            ancReader.setDecodeDolbyE(loadOptionsPanel.isDecodingDolbyE());
            ancReader.setDecodeAncillaryPackets(true);
            ancReader.setMetadataReadMode(MxfReader.MetadataReadMode.All);
            ancReader.setParseEssenceElements(true);
            ancReader.setParseSystemElements(true);
            info("scanning for data from " + ancReader.getFile());
            try {
                ancReader.readAll();
            } catch (Exception e) {
                error("Error scanning for data from " + ancReader.getFile(), e);
                error("Read to " + ancReader.getStreamOffset(), null);
            }
        }
    }

    private boolean isVancVbiEssenceDescriptor(EssenceTrack essenceTrack) {
        return (essenceTrack.getTrackNumber() == EssenceFileOutputController.SMPTE436_VBI_TRACK
                || essenceTrack.getTrackNumber() == EssenceFileOutputController.SMPTE436_ANC_TRACK);
    }

    private boolean isSoundEssenceDescriptor(EssenceTrack essenceTrack) {
        GroupNode descriptor = essenceTrack.getFilePackage().ref(Metadata.EssenceDescription);
        if (descriptor == null) {
            return false;
        }
        List<GroupNode> descriptors = new ArrayList<GroupNode>();
        if (descriptor.ul().equals(Groups.MultipleDescriptor)) {
            for (GroupNode subDescriptor : descriptor.refs(Metadata.FileDescriptors)) {
                descriptors.add(subDescriptor);
            }
        } else {
            descriptors.add(descriptor);
        }
        for (GroupNode subDescriptor : descriptors) {
            if (subDescriptor.ul().equals(Groups.Aes3Descriptor)
                    || subDescriptor.ul().equals(Groups.WaveAudioEssenceDescriptor)
                    || subDescriptor.ul().equals(Groups.WaveAudioPhysicalDescriptor)
                    || subDescriptor.ul().equals(Groups.GenericSoundEssenceDescriptor)) {
                return true; //TODO: also look if track is actually a "sound" type track
            }
        }
        return false;
    }

    private void buildExportableTrackList() {
        exportableTrackList.clear();
        parseGroupsForEssenceContainers(getFile(), getGroups());
        if (loadOptionsPanel.isFollowExternalReferences()) {
            for (File file : getExternalFileList()) {
                MxfTreeReader reader = new MxfTreeReader(file);
//                reader.setReadHeaderOnly(true);
                try {
                    reader.readAll();
                } catch (Exception e) {
                    error("Error parsing external file " + file, e);
                    continue;
                }
                parseGroupsForEssenceContainers(file, reader.getGroups());
            }
        }
    }

    private void parseGroupsForEssenceContainers(File file, Set<GroupNode> groups) {
        for (Iterator<GroupNode> i = groups.iterator(); i.hasNext();) {
            GroupNode group = i.next();
            if (Groups.EssenceContainerData.equals(group.ul())) {
                Number bodySid = (Number) group.value(Metadata.EssenceStreamID);
                UMID filePackageId = (UMID) group.value(Metadata.LinkedPackageID);
                addTracks(file, groups, bodySid, filePackageId);
            }
        }
    }

    private void addTracks(File file, Set<GroupNode> groups, Number bodySid, UMID filePackageId) {
        for (Iterator<GroupNode> i = groups.iterator(); i.hasNext();) {
            GroupNode group = i.next();
            if (Groups.SourcePackage.equals(group.ul())) {
                GroupNode descriptor = group.ref(Metadata.EssenceDescription);
                if (descriptor == null || descriptor.find(Metadata.EssenceLocators) != null) {
                    continue;
                }
                UMID packageId = (UMID) group.value(Metadata.PackageID);
                if (packageId.equals(filePackageId)) {
                    LeafNode tracksNode = (LeafNode) group.find(Metadata.Tracks);
                    for (Iterator<GroupNode> t = tracksNode.refs().iterator(); t.hasNext();) {
                        GroupNode track = t.next();
                        // SMPTE 377 says to treat non-zero track numbers in Lower-level Source
                        // Packages (and Material Packages) as "dark" metadata; however we're
                        // currently looking at Source Packages that are referenced as a File
                        // Package from the Essence Container Data set, so we're ok
                        long trackNumber = ((Number)track.value(Metadata.TrackNumber)).longValue();
                        if (trackNumber == 0) {
                            continue;
                        }
                        exportableTrackList.add(
                                new EssenceTrack(file, bodySid.longValue(), trackNumber, group, track));
                    }
                }
            }
        }
    }

    void setLength(long length)
    {
        adjusting = true;
        scanBar.setMinimum(0);
        scanBar.setMaximum((int) (length / 1000));
        adjusting = false;
    }

    void setPosition(float pos)
    {
        int position = (int)(scanBar.getMaximum() * pos);
        adjusting = true;
        scanBar.setValue(position);
        adjusting = false;
    }

    void setTime(long time)
    {
        adjusting = true;
        scanBar.setValue((int) (time / 1000));
        adjusting = false;
    }

    void setFinished()
    {
        playButton.setSelected(false);
    }

    public void setVideoSize(Dimension videoDimension)
    {
        canvas.setSize(videoDimension);
        frame.pack();
    }

    private void start(String mediaPath)
    {
        // Play a particular item, with options if necessary
        String[] mediaOptions = {
//                "--video-filter=scale{0.2}",
//                "--scale=0.2",
        };
        debug("playing...");
//        mediaPlayer.playMedia(mediaPath, mediaOptions);
    }

    public void actionPerformed(ActionEvent e)
    {
        frame.setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if (e.getSource() == playButton) {
    //            if (!mediaPlayer.isPlayable() && playButton.isSelected()) {
    //                start(path);
    //            } else if (!mediaPlayer.isPlaying() && playButton.isSelected()) {
    //                mediaPlayer.play();
    //            } else if (!playButton.isSelected()) {
    //                mediaPlayer.pause();
    //            }
            } else if (e.getSource() == loadMenuItem) {
                loadMenuItemAction();
            } else if (e.getSource() == exportEssenceMenuItem) {
                exportEssenceMenuItemAction();
            }
        } finally {
            frame.setCursor( Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void exportEssenceMenuItemAction() {
        ExportPanel exportPanel = new ExportPanel(this);
        int result = JOptionPane.showConfirmDialog(
                frame, exportPanel, "Export Essence", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        MxfExportReader exportReader = new MxfExportReader(reader.getFile(), this,
                exportPanel.getSelectedTracks(), exportPanel.getCreateCaptionOnlyFile(),
                exportPanel.isCaptionFileZeroBased());
        exportReader.setMetadataReadMode(MxfReader.MetadataReadMode.All);
        exportReader.setParseEssenceElements(true);
//        exportReader.setValidateEssenceTrack(true);
        info("exporting " + exportReader.getFile());
        try {
            exportReader.readAll();
        } catch (Exception e) {
            error("Error exporting " + exportReader.getFile(), e);
            error("Read to " + exportReader.getStreamOffset(), null);
        }
        if (loadOptionsPanel.isFollowExternalReferences()) {
            for (File file : getExternalFileList()) {
                exportReader = new MxfExportReader(file, this,
                        exportPanel.getSelectedTracks(), exportPanel.getCreateCaptionOnlyFile(),
                        exportPanel.isCaptionFileZeroBased());
                exportReader.setParseEssenceElements(true);
                info("exporting " + exportReader.getFile());
                try {
                    exportReader.readAll();
                } catch (Exception e) {
                    error("Error exporting external file " + file, e);
                    error("Read to " + exportReader.getStreamOffset(), null);
                }
            }
        }
    }

    private void loadMenuItemAction() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        ArrayList<File> list = new ArrayList<File>();
        list.add(fc.getSelectedFile());
        dropFile(list, loadMenuItem);
    }

    public boolean isDebug() {
        return debug;
    }

    public void debug(String message) {
        log.debug(message);
        if (!debug) {
            return;
        }
        loggerPanel.debug(message);
    }

    public void info(String message) {
        log.info(message);
        loggerPanel.info(message);
    }

    public void warn(String message) {
        log.warn(message);
        loggerPanel.warn(message);
    }

    public void error(String message, Exception e) {
        log.error(message, e);
        loggerPanel.error(message, e);
        overviewPanel.setSelectedIndex(0);
        SwingUtilities.invokeLater(new ShowLoggerErrorRunnable(loggerPanel));
    }
}
