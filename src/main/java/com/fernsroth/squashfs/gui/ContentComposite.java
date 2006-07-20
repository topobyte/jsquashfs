/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.Manifest;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class ContentComposite extends SashForm {

    /**
     * tree view showing the folders.
     */
    private FolderTree folderTree;

    /**
     * file table showing the files in the selected folder.
     */
    private FileTable fileTable;

    /**
     * @param parent the parent shell.
     * @param style the style.
     */
    public ContentComposite(Shell parent, int style) {
        super(parent, style | SWT.HORIZONTAL);
        this.setLayout(new FillLayout());

        this.folderTree = new FolderTree(this, SWT.BORDER) {

            /**
             * {@inheritDoc}
             */
            @Override
            protected void onTreeItemSelected(SelectionEvent event) {
                super.onTreeItemSelected(event);
                TreeItem[] selection = this.getSelection();
                if (selection != null && selection.length > 0) {
                    selectItem((Directory) selection[0].getData(), null);
                }
            }
        };

        this.fileTable = new FileTable(this, SWT.BORDER | SWT.FULL_SELECTION
                | SWT.MULTI);
        this.fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                for (TableItem ti : ContentComposite.this.fileTable
                        .getSelection()) {
                    if (ti.getData() instanceof Directory) {
                        ContentComposite.this.selectItem((Directory) ti
                                .getData(), null);
                    }
                }
            }

        });

        this.setWeights(new int[] { 20, 80 });

        loadManifest(null);
    }

    /**
     * reload the data in the file table and folder tree.
     */
    /* TODO is this function needed?
     private void reload() {
     int[] fileTableSelection = this.fileTable.getSelectionIndices();
     TreeItem[] folderTreeSelection = this.folderTree.getSelection();
     Directory[] folderTreeBaseFileSelection = new Directory[folderTreeSelection.length];
     for (int i = 0; i < folderTreeSelection.length; i++) {
     folderTreeBaseFileSelection[i] = (Directory) folderTreeSelection[i]
     .getData();
     }

     loadManifest(this.loadedManifest);

     folderTreeSelection = this.folderTree.findTreeItems(
     this.folderTree.getItems()[0], folderTreeBaseFileSelection)
     .toArray(new TreeItem[] {});
     for (TreeItem ti : folderTreeSelection) {
     this.folderTree.expandToItem(ti);
     }
     this.folderTree.setSelection(folderTreeSelection);
     if (folderTreeSelection.length > 0) {
     selectItem(folderTreeBaseFileSelection[0], null);
     }
     this.fileTable.setSelection(fileTableSelection);
     }
     */

    /**
     * select an item from the tree.
     * @param dir the directory to select.
     * @param treeItem the tree item the directory is attached to. null, if not known. 
     */
    private void selectItem(Directory dir, TreeItem treeItem) {
        // find tree item.
        if (treeItem == null) {
            List<TreeItem> treeItems = this.folderTree.findTreeItems(
                    this.folderTree.getItems()[0], new Directory[] { dir });
            if (treeItems.size() > 0) {
                treeItem = treeItems.get(0);
            }
        }
        if (treeItem == null) {
            throw new RuntimeException("tree item not found");
        }

        // expand tree to tree item
        this.folderTree.expandToItem(treeItem);

        // show selection in the file view.
        this.fileTable.setItems(dir.getSubentries());
        this.folderTree.setSelection(new TreeItem[] { treeItem });
    }

    /**
     * @param manifest
     */
    public void loadManifest(Manifest manifest) {
        this.folderTree.removeAll();
        this.fileTable.removeAll();
        if (manifest != null) {
            this.folderTree.setRoot(manifest.getRoot());
        }
    }
}
