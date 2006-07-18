/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.fernsroth.easyio.EasyIORandomAccessFile;
import com.fernsroth.squashfs.OutputWalkHandler;
import com.fernsroth.squashfs.SquashFSGlobals;
import com.fernsroth.squashfs.SquashFSManifest;
import com.fernsroth.squashfs.SquashFSReader;
import com.fernsroth.squashfs.SquashFSUtils;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.Manifest;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class JSquashFSGUI {
    /**
     * new file.
     */
    private static final File NEW_FILE = new File("new");

    /**
     * prefix for recent files preferences.
     */
    private static final String PREF_RECENT_PREFIX = "recent";

    /**
     * number of recent files.
     */
    private static final int RECENT_FILE_COUNT = 4;

    /**
     * the recent file list index from the top of the file menu.
     */
    private static final int RECENT_FILE_LIST_INDEX = 4;

    /**
     * 
     */
    private final Cursor CURSOR_HOURGLASS;

    /**
     * 
     */
    private final Cursor CURSOR_ARROW;

    /**
     * logging.
     */
    private static Log log = LogFactory.getLog(JSquashFSGUI.class);

    /**
     * the SWT display.
     */
    private Display display;

    /**
     * the SWT shell.
     */
    private Shell shell;

    /**
     * the content window.
     */
    private ContentComposite content;

    /**
     * the menu bar.
     */
    private Menu menu;

    /**
     * the file menu.
     */
    private Menu menuFile;

    /**
     * the currently loaded manifest.
     */
    private Manifest manifest;

    /**
     * the current working file.
     */
    private File workingFile;

    /**
     * the modified status.
     */
    private boolean modified;

    /**
     * constructor.
     * @param args the command line arguments.
     */
    public JSquashFSGUI(String[] args) {
        this.display = new Display();
        this.CURSOR_HOURGLASS = new Cursor(this.display, SWT.CURSOR_WAIT);
        this.CURSOR_ARROW = new Cursor(this.display, SWT.CURSOR_ARROW);
        this.shell = new Shell(this.display);
        this.shell.setLayout(new FillLayout());

        setWorkingFileName(null, false);

        createMenu();

        this.content = new ContentComposite(this.shell, 0);

        this.shell.open();
        while (!this.shell.isDisposed()) {
            try {
                if (!this.display.readAndDispatch()) {
                    this.display.sleep();
                }
            } catch (Throwable e) {
                log.error("error", e);
                MessageBox mb = new MessageBox(this.shell, SWT.OK
                        | SWT.ICON_ERROR);
                mb.setText("Critical Error");
                mb.setMessage("Critical Error\n\n"
                        + GUIUtils.exceptionToString(e));
                mb.open();
            }
        }
        this.display.dispose();
    }

    /**
     * create the menu bar.
     */
    private void createMenu() {
        MenuItem mi;

        this.menu = new Menu(this.shell, SWT.BAR);

        this.menuFile = new Menu(this.shell, SWT.DROP_DOWN);
        mi = new MenuItem(this.menu, SWT.CASCADE);
        mi.setText("&File");
        mi.setMenu(this.menuFile);

        mi = new MenuItem(this.menuFile, SWT.PUSH);
        mi.setText("New\tCtrl+N");
        mi.setAccelerator(SWT.CTRL | 'N');
        mi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onNew();
            }
        });

        mi = new MenuItem(this.menuFile, SWT.PUSH);
        mi.setText("Open...\tCtrl+O");
        mi.setAccelerator(SWT.CTRL | 'O');
        mi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onOpen();
            }
        });

        mi = new MenuItem(this.menuFile, SWT.PUSH);
        mi.setText("Close\tAlt+F4");
        mi.setAccelerator(SWT.ALT | SWT.F4);
        mi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onClose();
            }
        });

        mi = new MenuItem(this.menuFile, SWT.SEPARATOR);

        mi = new MenuItem(this.menuFile, SWT.SEPARATOR);

        mi = new MenuItem(this.menuFile, SWT.PUSH);
        mi.setText("E&xit\tAlt+F4");
        mi.setAccelerator(SWT.ALT | SWT.F4);
        mi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                JSquashFSGUI.this.shell.dispose();
            }
        });

        this.shell.setMenuBar(this.menu);

        updateRecentFilesMenu();
    }

    /**
     * open a recent file.
     * @param mi the menu item containing the recent file.
     */
    protected void openRecentFile(MenuItem mi) {
        // TODO Auto-generated method stub

    }

    /**
     * called when the user tries to close the current document.
     */
    protected void onClose() {
        closeWorkingFile();
    }

    /**
     * 
     */
    protected void onOpen() {
        if (!closeWorkingFile()) {
            return;
        }

        FileDialog fd = new FileDialog(this.shell, SWT.OPEN);
        fd.setText("Open...");
        fd.setFilterExtensions(new String[] { "*.*", "*.xml", "*.mo" });
        fd.setFilterNames(new String[] { "All Files (*.*)",
                "Manifest Files (*.xml)", "Modules (*.mo)" });
        String fileName;
        if ((fileName = fd.open()) != null) {
            this.shell.setCursor(this.CURSOR_HOURGLASS);
            File f = new File(fileName);

            try {
                Manifest man = SquashFSManifest.load(new FileInputStream(f), f
                        .getParentFile());
                this.content.loadManifest(man);
                setWorkingFileName(f, false);
            } catch (Exception e1) {
                try {
                    File destFile = File.createTempFile("jsquashfs", "dir");
                    destFile.delete();
                    destFile.mkdirs();
                    SquashFSReader reader = new SquashFSReader(
                            new EasyIORandomAccessFile(f, "r"));
                    OutputWalkHandler out = new OutputWalkHandler(reader,
                            destFile);
                    out.setIncludeMTimeInManfest(false);
                    SquashFSUtils.walk(reader.getRootDirectory(), out);
                    File manifestFile = new File(destFile, "manifest.xml");
                    out.writeManifest(manifestFile);

                    Manifest man = SquashFSManifest.load(new FileInputStream(
                            manifestFile), manifestFile.getParentFile());
                    this.content.loadManifest(man);
                    setWorkingFileName(f, false);
                } catch (Exception e2) {
                    MessageBox mb = new MessageBox(this.shell, SWT.OK
                            | SWT.ICON_ERROR);
                    mb.setText("Error Loading");
                    mb.setMessage("Error Loading\n" + e2.getMessage());
                    mb.open();
                }
            } finally {
                this.shell.setCursor(this.CURSOR_ARROW);
            }
        }
    }

    /**
     * sets the working file name.
     * @param workingFile the working file.
     * @param modified the modified status.
     */
    private void setWorkingFileName(File workingFile, boolean modified) {
        this.workingFile = workingFile;
        this.modified = modified;
        StringBuffer txt = new StringBuffer();
        txt.append(SquashFSGlobals.PROJECT_NAME);
        txt.append(" GUI");
        if (workingFile != null) {
            txt.append(" - ");
            txt.append(workingFile.getAbsolutePath());
            if (modified) {
                txt.append("*");
            }
        }
        this.shell.setText(txt.toString());
    }

    /**
     * creates a new file. 
     */
    protected void onNew() {
        if (!closeWorkingFile()) {
            return;
        }

        this.manifest = new Manifest();
        this.manifest.setRoot(new Directory(null, 0, 0, 0, 0));

        this.content.loadManifest(this.manifest);
        setWorkingFileName(NEW_FILE, false);
    }

    /**
     * check that the current working file is saved. 
     * @return true, if it's ok to close. false, if not.
     */
    private boolean closeWorkingFile() {
        if (isModified()) {
            MessageBox mb = new MessageBox(this.shell, SWT.YES | SWT.NO
                    | SWT.ICON_QUESTION);
            mb.setText("Confirm Close");
            mb
                    .setMessage("The current file has been modified, are you sure you want to close?");
            if (mb.open() != SWT.YES) {
                return false;
            }
        }
        if (this.workingFile != null) {
            addRecentFile(this.workingFile);
        }
        setWorkingFileName(null, false);
        this.content.loadManifest(null);

        updateRecentFilesMenu();

        return true;
    }

    /**
     * 
     */
    private void updateRecentFilesMenu() {
        List<File> recentFileList = getRecentFileList();

        // remove old items
        for (MenuItem mi : this.menuFile.getItems()) {
            if (mi.getData() instanceof File) {
                mi.dispose();
            }
        }

        // add new items.
        int i = RECENT_FILE_LIST_INDEX;
        for (File f : recentFileList) {
            MenuItem mi = new MenuItem(this.menuFile, SWT.PUSH, i++);
            mi.setText(f.getAbsolutePath());
            mi.setData(f);
            mi.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    openRecentFile((MenuItem) e.getSource());
                }
            });
        }
    }

    /**
     * adds a file to the recent working list.
     * @param f the file to add.
     */
    private void addRecentFile(File f) {
        if (f == NEW_FILE) {
            return;
        }
        Preferences pref = Preferences.userRoot();

        // create new list
        List<File> recentFileList = getRecentFileList();
        recentFileList.add(0, f);

        // remove duplicates
        Set<String> newList = new LinkedHashSet<String>();
        for (File file : recentFileList) {
            newList.add(file.getAbsolutePath());
        }
        recentFileList.clear();
        for (String str : newList) {
            recentFileList.add(new File(str));
        }

        // set preferences.
        int i = 0;
        for (File recentFile : recentFileList) {
            pref.put(PREF_RECENT_PREFIX + Integer.toString(i++), recentFile
                    .getAbsolutePath());
        }
        pref.put(PREF_RECENT_PREFIX + Integer.toString(i++), "");
    }

    /**
     * gets the recent file list.
     * @return the recent file list.
     */
    private List<File> getRecentFileList() {
        Preferences pref = Preferences.userRoot();
        List<File> results = new ArrayList<File>();
        for (int i = 0; i < RECENT_FILE_COUNT; i++) {
            String t = pref.get(PREF_RECENT_PREFIX + Integer.toString(i), null);
            if (t == null || "".equals(t)) {
                break;
            }
            File f = new File(t);
            results.add(f);
        }
        return results;
    }

    /**
     * check if the current document has been modified.
     * @return true, if is modified. false, if not.
     */
    private boolean isModified() {
        return this.modified;
    }

    /**
     * main entry point.
     * @param args the command line arguments.
     */
    public static void main(String args[]) {
        new JSquashFSGUI(args);
    }
}
