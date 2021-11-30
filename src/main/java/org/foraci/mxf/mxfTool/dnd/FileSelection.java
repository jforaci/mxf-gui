package org.foraci.mxf.mxfTool.dnd;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

/**
 * A <code>Transferable</code> for packaging a list of <code>File</code>s
 *
 * @author jforaci
 */
public class FileSelection implements Transferable
{
    private static final Logger log = LoggerFactory.getLogger(FileSelection.class.getName());
    public static DataFlavor uriListReaderFlavor, uriListInputStreamFlavor, uriListStringFlavor, gnomeIconList, gnomeFileList;

    static {
        try {
            uriListReaderFlavor = new DataFlavor("text/uri-list; class=java.io.Reader");
//            uriListInputStreamFlavor = new DataFlavor("text/uri-list; class=java.io.InputStream");
            uriListInputStreamFlavor = new DataFlavor("text/uri-list");
            uriListStringFlavor = new DataFlavor("text/uri-list; class=java.lang.String");
//            gnomeIconList = new DataFlavor("x-special/gnome-icon-list; class=java.io.InputStream");
//            gnomeFileList = new DataFlavor("x-special/gnome-copied-files");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static final DataFlavor[] flavors = {
        DataFlavor.javaFileListFlavor,
        uriListReaderFlavor,
        uriListInputStreamFlavor,
        uriListStringFlavor,
//        gnomeIconList,
//        gnomeFileList,
    };

    private List<File> files;

    public FileSelection(List<File> files)
    {
        if (files == null) {
            throw new NullPointerException("files can not be null");
        }
        this.files = files;
    }

    /**
     * Returns an array of flavors in which this <code>Transferable</code>
     * can provide the data.
     *
     * @return an array of <code>DataFlavor</code>s
     */
    public DataFlavor[] getTransferDataFlavors() {
	    return (DataFlavor[])flavors.clone();
    }

    /**
     * Returns whether the requested flavor is supported by this
     * <code>Transferable</code>.
     *
     * @param flavor the requested flavor for the data
     * @return true if <code>flavor</code> is equal to
     *   <code>DataFlavor.javaFileListFlavor</code>; false otherwise
     * @throws NullPointerException if flavor is <code>null</code>
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        log.debug("ASKING " + flavor);
        for (int i = 0; i < flavors.length; i++) {
            if (flavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the <code>Transferable</code>'s data in the requested
     * <code>DataFlavor</code> if possible.
     *
     * @param flavor the requested flavor for the data
     * @return the data in the requested flavor, as outlined above
     * @throws java.awt.datatransfer.UnsupportedFlavorException if the requested data flavor is
     *         not known
     * @throws NullPointerException if flavor is <code>null</code>
     */
    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException
    {
        log.debug(FileSelection.class.getName() + ".getTransferData flavor: " + flavor);
        if (flavor.equals(DataFlavor.javaFileListFlavor)) {
            return (Object)files;
        } else if (flavor.equals(uriListReaderFlavor)) {
            String s = getUriListString();
            return new StringReader(s);
        } else if (flavor.equals(uriListInputStreamFlavor)) {
            String s = getUriListString();
            return new ByteArrayInputStream(s.getBytes());
        } else if (flavor.equals(uriListStringFlavor)) {
            return getUriListString();
//        } else if (flavor.equals(gnomeIconList)) {
//            log.debug("GNOME ICON LIST");
//            String s = getStringList();
//            return s;
//        } else if (flavor.equals(gnomeFileList)) {
//            log.debug("GNOME FILE COPY");
//            String s = getStringList();
//            return new ByteArrayInputStream(s.getBytes());
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    private String getUriListString()
    {
        StringBuilder w = new StringBuilder();
        for (Iterator<File> i = files.iterator(); i.hasNext();) {
            File f = i.next();
            w.append(f.toURI().toString());
            w.append("\r\n");
        }
        String s = w.toString();
        log.debug("URI list: " + s);
        return s;
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
