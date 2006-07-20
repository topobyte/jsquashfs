/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.fernsroth.squashfs.SquashFSUtils;
import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.SFSFile;
import com.fernsroth.squashfs.model.SFSSourceFile;
import com.fernsroth.squashfs.model.SymLink;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class FileTable extends Table {
    /**
     * the name column index.
     */
    protected static final int COLUMN_NAME = 0;

    /**
     * the source column index.
     */
    protected static final int COLUMN_SOURCE = 1;

    /**
     * the guid column index.
     */
    protected static final int COLUMN_GUID = 2;

    /**
     * the uid column index.
     */
    protected static final int COLUMN_UID = 3;

    /**
     * the mode column index.
     */
    protected static final int COLUMN_MODE = 4;

    /**
     * the mtime column index.
     */
    protected static final int COLUMN_MTIME = 5;

    /**
     * file icon.
     */
    private Image fileImage;

    /**
     * symlink icon.
     */
    private Image linkImage;

    /**
     * a closed folder image.
     */
    private Image folderImageClosed;

    /**
     * the list of table items.
     */
    private List<BaseFile> tableItems;

    /**
     * the table comparator for sorting on column click.
     */
    private TableComparator tableComparator = new TableComparator();

    /**
     * @param parent the parent window.
     * @param style additional styles.
     */
    public FileTable(Composite parent, int style) {
        super(parent, style);

        InputStream in = getClass().getResourceAsStream("file.ico");
        this.fileImage = new Image(parent.getDisplay(), new ImageData(in));

        in = getClass().getResourceAsStream("link.ico");
        this.linkImage = new Image(parent.getDisplay(), new ImageData(in));

        in = getClass().getResourceAsStream("closedFolder.ico");
        this.folderImageClosed = new Image(parent.getDisplay(), new ImageData(
                in));

        TableColumn tc;
        tc = new TableColumn(this, SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(100);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                FileTable.COLUMN_NAME));

        tc = new TableColumn(this, SWT.LEFT);
        tc.setText("Source File");
        tc.setWidth(250);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                FileTable.COLUMN_GUID));

        tc = new TableColumn(this, SWT.LEFT);
        tc.setText("Group ID");
        tc.setWidth(60);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                FileTable.COLUMN_GUID));

        tc = new TableColumn(this, SWT.LEFT);
        tc.setText("User ID");
        tc.setWidth(60);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                FileTable.COLUMN_UID));

        tc = new TableColumn(this, SWT.LEFT);
        tc.setText("Mode");
        tc.setWidth(75);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                FileTable.COLUMN_MODE));

        tc = new TableColumn(this, SWT.LEFT);
        tc.setText("Modified Time");
        tc.setWidth(125);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                FileTable.COLUMN_MTIME));

        setHeaderVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (this.fileImage != null) {
            this.fileImage.dispose();
        }
        if (this.linkImage != null) {
            this.linkImage.dispose();
        }
        if (this.folderImageClosed != null) {
            this.folderImageClosed.dispose();
        }
        super.dispose();
    }

    /**
     * reload the table items.
     */
    private void reloadTable() {
        this.removeAll();
        if (this.tableItems == null) {
            return;
        }

        Collections.sort(this.tableItems, this.tableComparator);

        TableItem ti;
        for (BaseFile bf : this.tableItems) {
            ti = new TableItem(this, 0);
            ti.setData(bf);
            ti.setText(FileTable.COLUMN_NAME, bf.getName());
            if (bf instanceof SFSSourceFile) {
                SFSSourceFile sf = (SFSSourceFile) bf;
                String source = sf.getSourceFile() == null ? "" : sf
                        .getSourceFile().toString();
                ti.setText(FileTable.COLUMN_SOURCE, source);
            }
            ti.setText(FileTable.COLUMN_GUID, Long.toString(bf.getGuid()));
            ti.setText(FileTable.COLUMN_UID, Long.toString(bf.getUid()));
            ti.setText(FileTable.COLUMN_MODE, SquashFSUtils.getModeString(bf));
            ti.setText(FileTable.COLUMN_MTIME, GUIUtils.formatMTime(bf
                    .getMTime()));
            if (bf instanceof Directory) {
                ti.setImage(this.folderImageClosed);
            } else if (bf instanceof SFSFile) {
                ti.setImage(this.fileImage);
            } else if (bf instanceof SymLink) {
                ti.setImage(this.linkImage);
            }
        }
    }

    /**
     * sets the items in the table.
     * @param tableItems the items.
     */
    public void setItems(List<BaseFile> tableItems) {
        this.removeAll();

        this.tableItems = new ArrayList<BaseFile>();
        this.tableItems.addAll(tableItems);
        reloadTable();
    }

    /**
     * comparator for sorting file table items.
     */
    protected static class TableComparator implements Comparator<BaseFile> {

        /**
         * the sort direction constants.
         */
        public enum Direction {
            /**
             * assending sort direction. 
             */
            ASSENDING,

            /**
             * desending sort direction. 
             */
            DESENDING
        }

        /**
         * the sort column.
         */
        private int column = FileTable.COLUMN_NAME;

        /**
         * the sort direction.
         */
        private Direction direction = Direction.ASSENDING;

        /**
         * {@inheritDoc}
         */
        public int compare(BaseFile bf1, BaseFile bf2) {
            int result = compareFields(bf1, bf2);
            if (this.direction == Direction.DESENDING) {
                result = -result;
            }
            return result;
        }

        /**
         * compares just the fields.
         * @param bf1 {@link TableComparator#compare(BaseFile, BaseFile)}.
         * @param bf2 {@link TableComparator#compare(BaseFile, BaseFile)}.
         * @return {@link TableComparator#compare(BaseFile, BaseFile)}.
         */
        private int compareFields(BaseFile bf1, BaseFile bf2) {
            switch (this.column) {
            case FileTable.COLUMN_NAME:
                if (bf1 instanceof Directory && !(bf2 instanceof Directory)) {
                    return -1;
                }
                if (!(bf1 instanceof Directory) && bf2 instanceof Directory) {
                    return 1;
                }
                return bf1.getName().compareTo(bf2.getName());
            case FileTable.COLUMN_GUID:
                if (bf1.getGuid() < bf2.getGuid()) {
                    return -1;
                } else if (bf1.getGuid() > bf2.getGuid()) {
                    return 1;
                }
                return 0;
            case FileTable.COLUMN_UID:
                if (bf1.getUid() < bf2.getUid()) {
                    return -1;
                } else if (bf1.getUid() > bf2.getUid()) {
                    return 1;
                }
                return 0;
            case FileTable.COLUMN_MODE:
                break;
            case FileTable.COLUMN_MTIME:
                if (bf1.getMTime() < bf2.getMTime()) {
                    return -1;
                } else if (bf1.getMTime() > bf2.getMTime()) {
                    return 1;
                }
                return 0;
            }
            return 0;
        }

        /**
         * reverse the sort order.
         */
        public void reverseSortOrder() {
            if (this.direction == Direction.ASSENDING) {
                this.direction = Direction.DESENDING;
            } else {
                this.direction = Direction.ASSENDING;
            }
        }

        /**
         * sets the sort column.
         * @param column the sort column.
         */
        public void setColumn(int column) {
            if (this.column != column) {
                this.direction = Direction.ASSENDING;
            }
            this.column = column;
        }
    }

    /**
     * file table selection adapter.
     */
    protected class FileTableSelectionAdapter extends SelectionAdapter {

        /**
         * the column.
         */
        private int column;

        /**
         * @param column the column to sort on.
         */
        public FileTableSelectionAdapter(int column) {
            this.column = column;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetSelected(SelectionEvent e) {
            FileTable.this.tableComparator.setColumn(this.column);
            FileTable.this.tableComparator.reverseSortOrder();
            reloadTable();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkSubclass() {
        // allow subclass.
    }

}
