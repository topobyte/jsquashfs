/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.fernsroth.squashfs.SquashFSUtils;
import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.Manifest;
import com.fernsroth.squashfs.model.SFSFile;
import com.fernsroth.squashfs.model.SymLink;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class ContentComposite extends SashForm {

    /**
     * the name column index.
     */
    protected static final int COLUMN_NAME = 0;

    /**
     * the guid column index.
     */
    protected static final int COLUMN_GUID = 1;

    /**
     * the uid column index.
     */
    protected static final int COLUMN_UID = 2;

    /**
     * the mode column index.
     */
    protected static final int COLUMN_MODE = 3;

    /**
     * the mtime column index.
     */
    protected static final int COLUMN_MTIME = 4;

    /**
     * tree view showing the folders.
     */
    private Tree folderTree;

    /**
     * file table showing the files in the selected folder.
     */
    private Table fileTable;

    /**
     * a closed folder image.
     */
    private Image folderImageClosed;

    /**
     * a open folder image.
     */
    private Image folderImageOpen;

    /**
     * file icon.
     */
    private Image fileImage;

    /**
     * symlink icon.
     */
    private Image linkImage;

    /**
     * previously selected tree item.
     */
    private TreeItem previousSelectedTreeItem;

    /**
     * the list of table items.
     */
    private List<BaseFile> tableItems;

    /**
     * the table comparator for sorting on column click.
     */
    private TableComparator tableComparator = new TableComparator();

    /**
     * @param parent the parent shell.
     * @param style the style.
     */
    public ContentComposite(Shell parent, int style) {
        super(parent, style | SWT.HORIZONTAL);
        this.setLayout(new FillLayout());

        InputStream in = getClass().getResourceAsStream("closedFolder.ico");
        this.folderImageClosed = new Image(parent.getDisplay(), new ImageData(
                in));

        in = getClass().getResourceAsStream("openFolder.ico");
        this.folderImageOpen = new Image(parent.getDisplay(), new ImageData(in));

        in = getClass().getResourceAsStream("file.ico");
        this.fileImage = new Image(parent.getDisplay(), new ImageData(in));

        in = getClass().getResourceAsStream("link.ico");
        this.linkImage = new Image(parent.getDisplay(), new ImageData(in));

        this.folderTree = new Tree(this, SWT.BORDER);
        this.folderTree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onTreeItemSelected(e);
            }
        });
        this.folderTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Tree t = (Tree) e.widget;
                for (TreeItem ti : t.getSelection()) {
                    ti.setExpanded(!ti.getExpanded());
                }
            }
        });

        this.fileTable = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
        this.fileTable.setHeaderVisible(true);
        TableColumn tc;
        tc = new TableColumn(this.fileTable, SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(100);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                ContentComposite.COLUMN_NAME));

        tc = new TableColumn(this.fileTable, SWT.LEFT);
        tc.setText("Group ID");
        tc.setWidth(100);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                ContentComposite.COLUMN_GUID));

        tc = new TableColumn(this.fileTable, SWT.LEFT);
        tc.setText("User ID");
        tc.setWidth(100);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                ContentComposite.COLUMN_UID));

        tc = new TableColumn(this.fileTable, SWT.LEFT);
        tc.setText("Mode");
        tc.setWidth(100);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                ContentComposite.COLUMN_MODE));

        tc = new TableColumn(this.fileTable, SWT.LEFT);
        tc.setText("Modified Time");
        tc.setWidth(100);
        tc.addSelectionListener(new FileTableSelectionAdapter(
                ContentComposite.COLUMN_MTIME));

        this.setWeights(new int[] { 20, 80 });

        loadManifest(null);
    }

    /**
     * called when a tree item is selected.
     * @param event the event.
     */
    protected void onTreeItemSelected(SelectionEvent event) {
        if (event.item instanceof TreeItem) {
            TreeItem ti = (TreeItem) event.item;
            selectItem((Directory) ti.getData(), ti);
        }
    }

    /**
     * select an item from the tree.
     * @param dir the directory to select.
     * @param treeItem the tree item the directory is attached to. null, if not known. 
     */
    private void selectItem(Directory dir, TreeItem treeItem) {
        // find tree item.
        if (treeItem == null) {
            treeItem = findTreeItem(this.folderTree.getItems()[0], dir);
        }
        if (treeItem == null) {
            throw new RuntimeException("tree item not found");
        }

        // expand tree to tree item
        expandToItem(treeItem);

        // show selection in the file view.
        this.fileTable.removeAll();

        this.tableItems = new ArrayList<BaseFile>();
        this.tableItems.addAll(dir.getSubentries());
        reloadTable();

        if (this.previousSelectedTreeItem != null) {
            this.previousSelectedTreeItem.setImage(this.folderImageClosed);
        }
        treeItem.setImage(this.folderImageOpen);

        this.previousSelectedTreeItem = treeItem;
    }

    /**
     * reload the table items.
     */
    private void reloadTable() {
        this.fileTable.removeAll();
        if (this.tableItems == null) {
            return;
        }

        Collections.sort(this.tableItems, this.tableComparator);

        TableItem ti;
        for (BaseFile bf : this.tableItems) {
            ti = new TableItem(this.fileTable, 0);
            ti.setText(ContentComposite.COLUMN_NAME, bf.getName());
            ti.setText(ContentComposite.COLUMN_GUID, Long
                    .toString(bf.getGuid()));
            ti.setText(ContentComposite.COLUMN_UID, Long.toString(bf.getUid()));
            ti.setText(ContentComposite.COLUMN_MODE, SquashFSUtils
                    .getModeString(bf));
            ti.setText(ContentComposite.COLUMN_MTIME, formatDate(SquashFSUtils
                    .getDateFromMTime(bf.getMTime())));
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
     * formats a date for display.
     * @param date the date to format.
     * @return the string.
     */
    private String formatDate(Date date) {
        return new SimpleDateFormat().format(date);
    }

    /**
     * expands the tree to the specified item.
     * @param treeItem the item to expand to.
     */
    private void expandToItem(TreeItem treeItem) {
        TreeItem parentItem = treeItem.getParentItem();
        if (parentItem != null) {
            parentItem.setExpanded(true);
            expandToItem(parentItem);
        }
    }

    /**
     * find a directory in the tree.
     * @param rootItem the root item to start searching from.
     * @param dir the directory to find.
     * @return the tree item if found. null, if not found.
     */
    private TreeItem findTreeItem(TreeItem rootItem, Directory dir) {
        if (rootItem.getData() == dir) {
            return rootItem;
        }
        for (TreeItem child : rootItem.getItems()) {
            TreeItem found = findTreeItem(child, dir);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * @param manifest
     */
    public void loadManifest(Manifest manifest) {
        this.folderTree.removeAll();
        this.fileTable.removeAll();
        if (manifest != null) {
            TreeItem item = new TreeItem(this.folderTree, SWT.NONE);
            updateTreeItem(item, manifest.getRoot());
            addFolders(manifest.getRoot(), item);
            item.setExpanded(true);
        }
    }

    /**
     * adds folders to a tree item.
     * @param dir the directory to add.
     * @param parentItem the parent tree item.
     */
    private void addFolders(Directory dir, TreeItem parentItem) {
        for (BaseFile bf : dir.getSubentries()) {
            if (bf instanceof Directory) {
                Directory bfdir = (Directory) bf;
                TreeItem item = new TreeItem(parentItem, SWT.NONE);
                updateTreeItem(item, bfdir);
                addFolders(bfdir, item);
            }
        }
    }

    /**
     * @param item
     * @param dir
     */
    private void updateTreeItem(TreeItem item, Directory dir) {
        item.setText(dir.getName() == null ? "ROOT" : dir.getName());
        item.setData(dir);
        item.setImage(this.folderImageClosed);
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
        private int column = ContentComposite.COLUMN_NAME;

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
            case ContentComposite.COLUMN_NAME:
                if (bf1 instanceof Directory && !(bf2 instanceof Directory)) {
                    return -1;
                }
                if (!(bf1 instanceof Directory) && bf2 instanceof Directory) {
                    return 1;
                }
                return bf1.getName().compareTo(bf2.getName());
            case ContentComposite.COLUMN_GUID:
                if (bf1.getGuid() < bf2.getGuid()) {
                    return -1;
                } else if (bf1.getGuid() > bf2.getGuid()) {
                    return 1;
                }
                return 0;
            case ContentComposite.COLUMN_UID:
                if (bf1.getUid() < bf2.getUid()) {
                    return -1;
                } else if (bf1.getUid() > bf2.getUid()) {
                    return 1;
                }
                return 0;
            case ContentComposite.COLUMN_MODE:
                break;
            case ContentComposite.COLUMN_MTIME:
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
            ContentComposite.this.tableComparator.setColumn(this.column);
            ContentComposite.this.tableComparator.reverseSortOrder();
            reloadTable();
        }
    }
}
