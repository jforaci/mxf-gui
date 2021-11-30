package org.foraci.mxf.mxfTool.dnd;

import org.foraci.mxf.mxfTool.gui.MxfTool;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>TransferHandler</code> that imports a <code>File</code> list drop
 *
 * @author jforaci
 */
public class FileDropTransferHandler extends TransferHandler
{
    private static final Logger log = LoggerFactory.getLogger(FileDropTransferHandler.class.getName());

    private MxfTool owner;

    public FileDropTransferHandler(MxfTool owner)
    {
        this.owner = owner;
    }

    public boolean importData(JComponent comp, Transferable t)
    {
        if (!canImport(comp, t.getTransferDataFlavors())) {
            return false;
        }
        List data;
        try {
            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                data = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
            } else { // assume a Reader
                data = new ArrayList();
                try {
                    DataFlavor uriListReaderFlavor = new DataFlavor("text/uri-list; class=java.io.Reader");
                    BufferedReader r = new BufferedReader((Reader) t.getTransferData(uriListReaderFlavor));
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (line.trim().startsWith("#")) {
                            continue;
                        }
                        log.debug(line);
                        URI uri = new URI(line);
                        File file = new File(uri);
                        data.add(file);
                    }
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage(), e);
                } catch (URISyntaxException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (UnsupportedFlavorException ufe) {
            log.error("importData: unsupported data flavor", ufe);
            return false;
        } catch (IOException ioe) {
            log.error("importData: I/O exception", ioe);
            return false;
        }
        // drop/paste
        owner.dropFile(data, comp);
        return true;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
    {
        for (DataFlavor flavor : transferFlavors) {
            if (DataFlavor.javaFileListFlavor.equals(flavor)) {
                return true;
            }
            if (flavor.isMimeTypeEqual("text/uri-list")
                    && flavor.isRepresentationClassReader()) {
                return true;
            }
        }
        return false;
    }
}
