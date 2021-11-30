package org.foraci.mxf.mxfTool.dataMgrs.export;

import org.foraci.mxf.mxfReader.registries.Metadata;
import org.foraci.mxf.mxfTool.dataMgrs.EssenceTrack;
import org.foraci.mxf.mxfTool.dataMgrs.Utils;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for exporting tracks
 */
public class ExportTrackTableModel extends AbstractTableModel {
    public static final String[] COLUMN_NAMES = { "Include", "Track", "ID", "Partition", "File" };
    public static final int COLUMN_INCLUDE = 0;
    public static final int COLUMN_TRACK_NAME = 1;
    public static final int COLUMN_TRACK_ID = 2;
    public static final int COLUMN_PARTITION = 3;
    public static final int COLUMN_FILE = 4;

    private final List<EssenceTrack> tracks;
    private final List<Boolean> include;

    public ExportTrackTableModel(List<EssenceTrack> tracks) {
        this.tracks = tracks;
        this.include = new ArrayList<Boolean>(tracks.size());
        for (int i = 0; i < tracks.size(); i++) {
            this.include.add(Boolean.TRUE);
        }
    }

    public int getRowCount() {
        return tracks.size();
    }

    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case COLUMN_INCLUDE:
                return true;
            case COLUMN_TRACK_NAME:
            case COLUMN_TRACK_ID:
            case COLUMN_PARTITION:
            case COLUMN_FILE:
            default:
                return false;
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            return;
        }
        switch (columnIndex) {
            case COLUMN_INCLUDE:
                include.set(rowIndex, (Boolean) aValue);
                break;
        }
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLUMN_INCLUDE:
                return Boolean.class;
            case COLUMN_TRACK_NAME:
            case COLUMN_TRACK_ID:
            case COLUMN_FILE:
                return String.class;
            case COLUMN_PARTITION:
                return Long.class;
        }
        return super.getColumnClass(columnIndex);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        EssenceTrack exportTrack = tracks.get(rowIndex);
        switch (columnIndex) {
            case COLUMN_INCLUDE:
                return include.get(rowIndex);
            case COLUMN_TRACK_NAME:
                return Utils.getTrackLabel(exportTrack.getTrack());
            case COLUMN_TRACK_ID:
                return exportTrack.getTrack().string(Metadata.TrackID);
            case COLUMN_PARTITION:
                return exportTrack.getBodySid();
            case COLUMN_FILE:
                return exportTrack.getFile().getName();
        }
        return null;
    }

    private void include(boolean all) {
        for (int i = 0; i < include.size(); i++) {
            include.set(i, all);
        }
        fireTableDataChanged();
    }

    public void includeAll() {
        include(true);
    }

    public void includeNone() {
        include(false);
    }

    public List<EssenceTrack> getSelectedTracks() {
        List<EssenceTrack> list = new ArrayList<EssenceTrack>();
        for (int i = 0; i < getRowCount(); i++) {
            if (!include.get(i)) {
                continue;
            }
            EssenceTrack exportTrack = tracks.get(i);
            list.add(exportTrack);
        }
        return list;
    }
}
