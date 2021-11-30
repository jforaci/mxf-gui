package org.foraci.mxf.mxfTool.mxf;

import org.foraci.mxf.mxfReader.*;
import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.LeafNode;
import org.foraci.mxf.mxfReader.entities.Node;
import org.foraci.mxf.mxfReader.registries.Groups;
import org.foraci.mxf.mxfReader.registries.Metadata;
import org.foraci.mxf.mxfTool.dataMgrs.SMNode;
import org.foraci.mxf.mxfTool.dataMgrs.Utils;
import org.foraci.mxf.mxfTool.gui.MxfView;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

/**
 * Base reader for MXF assets uses <code>MxfTreeReader</code> in the MxfReader project
 */
public class MxfAssetReader extends MxfTreeReader {
    private MxfView view;

    public MxfAssetReader(File file, MxfView view) {
        super(file);
        this.view = view;
        setParseEssenceElements(true);
        setParseSystemElements(true);
    }

    @Override
    public void readAll() throws IOException {
        view.getPartitionPackList().clear();
        view.setRandomIndexPack(null);
        view.getExternalFileList().clear();
        super.readAll();
        DefaultMutableTreeNode parent = view.getStructureTreeRoot();
        for (Iterator<GroupNode> i = this.getGroups().iterator(); i.hasNext();) {
            GroupNode group = i.next();
            if (Groups.MaterialPackage.equals(group.ul())) {
                addPackage(parent, group);
            }
        }
        for (Iterator<GroupNode> i = this.getGroups().iterator(); i.hasNext();) {
            GroupNode group = i.next();
            if (Groups.SourcePackage.equals(group.ul())) {
                addPackage(parent, group);
            }
        }
        view.getPartitionPackList().addAll(getPartitionPacks());
    }

    private void addPackage(DefaultMutableTreeNode parent, GroupNode pkg) {
        boolean materialPackage = Groups.MaterialPackage.equals(pkg.ul());
        String label = (materialPackage) ? "Material Package" : "Source Package";
        DefaultMutableTreeNode child = addNode(parent, pkg, label);
        for (Iterator<Node> i = pkg.getChildren().iterator(); i.hasNext();) {
            Node node = i.next();
            if (Metadata.Tracks.equals(node.ul())) {
                for (Iterator<GroupNode> t = ((LeafNode)node).refs().iterator(); t.hasNext();) {
                    GroupNode trackNode = t.next();
                    addTrack(child, trackNode);
                }
            } else if (Metadata.EssenceDescription.equals(node.ul())) { // TODO: i guess we should make sure this is a source package too ...
                for (Iterator<GroupNode> d = ((LeafNode)node).refs().iterator(); d.hasNext();) {
                    GroupNode descriptorNode = d.next();
                    processDescriptor(descriptorNode);
                    addDescriptor(child, descriptorNode);
                }
            }
        }
    }

    private void addDescriptor(DefaultMutableTreeNode parent, GroupNode descriptorNode) {
        String label = descriptorNode.ul().getName();
        DefaultMutableTreeNode child = addNode(parent, descriptorNode, label);
        if (Groups.MultipleDescriptor.equals(descriptorNode.ul())) {
            List<GroupNode> refs = descriptorNode.refs(Metadata.FileDescriptors);
            if (refs != null) {
                for (GroupNode ref : refs) {
                    addDescriptor(child, ref);
                }
            }
        }
    }

    private void processDescriptor(GroupNode descriptorNode) {
        LeafNode locators = (LeafNode) descriptorNode.find(Metadata.EssenceLocators);
        if (locators == null) {
            return;
        }
        for (Iterator<GroupNode> t = locators.refs().iterator(); t.hasNext();) {
            GroupNode locatorNode = t.next();
            if (!Groups.NetworkLocator.equals(locatorNode.ul())) {
                continue;
            }
            String url = locatorNode.string(Metadata.URL);
            if (url == null) {
                url = locatorNode.string(Metadata.URL1);
            }
            if (url != null) {
                try {
                    url = url.replace('\\', '/');
                    URI uri = URI.create(url);
                    if (!uri.isAbsolute()) {
                        url = URLDecoder.decode(url, "ISO-8859-1");
                        File file = new File(getFile().getParentFile(), url);
                        view.getExternalFileList().add(file);
                        view.info("added external reference to " + url);
                        if (!file.exists()) {
                            view.warn("file " + url + " is missing");
                        }
                    } else if ("file".equals(uri.getScheme())) {
                        File file = new File(uri);
                        view.getExternalFileList().add(file);
                        view.info("added external reference to " + file);
                        if (!file.exists()) {
                            view.warn("file " + file + " is missing");
                        }
                    } else {
                        view.warn("unsupported network locator scheme: " + uri);
                    }
                } catch (IllegalArgumentException e) {
                    view.warn("found bad external reference: " + url);
                } catch (UnsupportedEncodingException e) {
                    view.warn("found bad external reference, could not decode: " + url);
                }
            }
        }
    }

    private void addTrack(DefaultMutableTreeNode parent, GroupNode track) {
        String label = Utils.getTrackLabel(track);
        DefaultMutableTreeNode child = addNode(parent, track, label);
    }

    @Override
    public void partitionPackRead(PartitionPack partitionPack) {
        super.partitionPackRead(partitionPack);
//        view.getPartitionPackList().add(partitionPack);
    }

    @Override
    public void randomIndexPackRead(RandomIndexPack randomIndexPack) {
        super.randomIndexPackRead(randomIndexPack);
        view.setRandomIndexPack(randomIndexPack);
    }

    @Override
    public void groupSetStarted(Key key) {
        super.groupSetStarted(key);
    }

    @Override
    public void groupSetEnded(Key key) {
        super.groupSetEnded(key);
    }

    private DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, GroupNode group, String label) {
        SMNode userObject = new SMNode(group, label);
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(userObject);
        parent.add(child);
        return child;
    }

    @Override
    public void valueRead(Key key, Object value) {
        super.valueRead(key, value);
    }

    @Override
    protected EssenceContainerOutputController createEssenceFileOutputController(File file) {
//        return new AncEssenceOutput(file, view);
        return view.getEssenceController();
    }

    @Override
    protected void debug(String message) {
        view.debug(message);
    }

    @Override
    protected void log(String message) {
        view.info(message);
    }

    @Override
    protected void warn(String message) {
        view.warn(message);
    }

    @Override
    protected void error(String message, Exception exception) {
        view.error(message, exception);
    }

    protected void dump(String line) {
//        super.dump(line);
        view.dump(line);
    }
}
