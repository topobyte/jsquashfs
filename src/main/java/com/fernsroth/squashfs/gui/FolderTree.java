/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.Directory;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class FolderTree extends Tree {

    /**
     * a closed folder image.
     */
    private Image folderImageClosed;

    /**
     * a open folder image.
     */
    private Image folderImageOpen;

    /**
     * the tree view editor.
     */
    private TreeEditor treeEditor;

    /**
     * previously selected tree item.
     */
    private TreeItem[] previousSelectedTreeItems;

    /**
     * a folder tree.
     * @param parent the parent window.
     * @param style additional styles to apply.
     */
    public FolderTree(Composite parent, int style) {
        super(parent, style);

        InputStream in = getClass().getResourceAsStream("closedFolder.ico");
        this.folderImageClosed = new Image(parent.getDisplay(), new ImageData(
                in));

        in = getClass().getResourceAsStream("openFolder.ico");
        this.folderImageOpen = new Image(parent.getDisplay(), new ImageData(in));

        addKeyListener(new KeyAdapter() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.keyCode == SWT.F2) {
                    Tree t = (Tree) e.widget;
                    for (TreeItem ti : t.getSelection()) {
                        editTreeItem(ti);
                        break;
                    }
                }
            }
        });

        addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editTreeItem(null);
                onTreeItemSelected(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            /**
             * the last mouse up item.
             */
            private TreeItem lastMouseUpItem;

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Tree t = (Tree) e.widget;
                for (TreeItem ti : t.getSelection()) {
                    ti.setExpanded(!ti.getExpanded());
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                Tree t = (Tree) e.widget;
                TreeItem clickedItem = t.getItem(new Point(e.x, e.y));
                if (clickedItem != null) {
                    if (this.lastMouseUpItem == clickedItem) {
                        editTreeItem(clickedItem);
                    }
                    this.lastMouseUpItem = clickedItem;
                } else {
                    editTreeItem(null);
                    this.lastMouseUpItem = null;
                }
            }
        });

        this.treeEditor = new TreeEditor(this);
        this.treeEditor.horizontalAlignment = SWT.LEFT;
        this.treeEditor.grabHorizontal = true;
        this.treeEditor.minimumWidth = 50;
    }

    /**
     * called when a tree item is selected.
     * @param event the event that occured.
     */
    protected void onTreeItemSelected(SelectionEvent event) {
        // do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (this.folderImageClosed != null) {
            this.folderImageClosed.dispose();
        }
        if (this.folderImageOpen != null) {
            this.folderImageOpen.dispose();
        }

        super.dispose();
    }

    /**
     * edit a tree item.
     * @param ti the tree item to edit. null, to stop editing.
     */
    protected void editTreeItem(TreeItem ti) {
        // Clean up any previous editor control
        Control oldEditor = this.treeEditor.getEditor();
        if (oldEditor != null) {
            TreeItem treeItem = this.treeEditor.getItem();
            Text text = (Text) this.treeEditor.getEditor();
            if (!validateEdit(treeItem, text.getText())) {
                text.setFocus();
                text.setSelection(0, text.getText().length());
                return;
            }
            performEdit(treeItem, text.getText());

            oldEditor.dispose();
        }

        // Identify the selected row
        if (ti == null) {
            return;
        }

        // The control that will be the editor must be a child of the Tree
        Text newEditor = new Text(this, SWT.SINGLE);
        newEditor.addKeyListener(new KeyAdapter() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.keyCode == 13) {
                    editTreeItem(null);
                }
            }
        });
        newEditor.setText(ti.getText());
        newEditor.selectAll();
        newEditor.setFocus();
        this.treeEditor.setEditor(newEditor, ti);
    }

    /**
     * perform the edit.
     * @param treeItem the tree item being edited.
     * @param text the new text.
     */
    protected void performEdit(TreeItem treeItem, String text) {
        treeItem.setText(text);
        Directory dir = (Directory) treeItem.getData();
        dir.setName(text);
    }

    /**
     * validate the tree item edit.
     * @param treeItem the tree item being edited.
     * @param text the new text.
     * @return true, if ok. false, if bad.
     */
    protected boolean validateEdit(TreeItem treeItem, String text) {
        TreeItem parentTreeItem = treeItem.getParentItem();

        // can't edit root name.
        if (parentTreeItem == null) {
            return false;
        }

        Directory parentDir = (Directory) parentTreeItem.getData();
        Directory dir = (Directory) treeItem.getData();

        // check for blank
        if (text.length() == 0) {
            MessageBox mb = new MessageBox(this.getShell(), SWT.OK
                    | SWT.ICON_ERROR);
            mb.setText("Error Naming File");
            mb.setMessage("Filename must contain at least one character.");
            mb.open();
            return false;
        }

        // check for duplicate name
        for (BaseFile childDir : parentDir.getSubentries()) {
            if (childDir != dir) {
                if (childDir.getName().equals(text)) {
                    MessageBox mb = new MessageBox(this.getShell(), SWT.OK
                            | SWT.ICON_ERROR);
                    mb.setText("Error Naming File");
                    mb.setMessage("File with name '" + text
                            + "' already exists.");
                    mb.open();
                    return false;
                }
            }
        }

        // TODO check valid unix name.

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelection(TreeItem[] items) {
        // unselect the previous selection.
        if (this.previousSelectedTreeItems != null) {
            for (TreeItem ti : this.previousSelectedTreeItems) {
                if (!ti.isDisposed()) {
                    ti.setImage(this.folderImageClosed);
                }
            }
        }

        super.setSelection(items);

        if (items != null) {
            for (TreeItem ti : items) {
                ti.setImage(this.folderImageOpen);
            }
        }

        this.previousSelectedTreeItems = items;
    }

    /**
     * find a directory in the tree.
     * @param rootItem the root item to start searching from.
     * @param dirs the datas to find.
     * @return the tree items found.
     */
    public List<TreeItem> findTreeItems(TreeItem rootItem, Directory dirs[]) {
        List<TreeItem> results = new ArrayList<TreeItem>();
        for (Directory dir : dirs) {
            if (rootItem.getData() == dir) {
                results.add(rootItem);
            }
        }
        for (TreeItem child : rootItem.getItems()) {
            List<TreeItem> found = findTreeItems(child, dirs);
            results.addAll(found);
        }
        return results;
    }

    /**
     * @param root
     */
    public void setRoot(Directory root) {
        TreeItem item = new TreeItem(this, SWT.NONE);
        updateTreeItem(item, root);
        addFolders(root, item);
        item.setExpanded(true);
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
     * expands the tree to the specified item.
     * @param treeItem the item to expand to.
     */
    public void expandToItem(TreeItem treeItem) {
        TreeItem parentItem = treeItem.getParentItem();
        if (parentItem != null) {
            parentItem.setExpanded(true);
            expandToItem(parentItem);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkSubclass() {
        // allow override.
    }
}
