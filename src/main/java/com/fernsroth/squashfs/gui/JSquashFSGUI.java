/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.io.File;
import java.io.FileInputStream;

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
     * constructor.
     * @param args the command line arguments.
     */
    public JSquashFSGUI(String[] args) {
        this.display = new Display();
        this.CURSOR_HOURGLASS = new Cursor(this.display, SWT.CURSOR_WAIT);
        this.CURSOR_ARROW = new Cursor(this.display, SWT.CURSOR_ARROW);
        this.shell = new Shell(this.display);
        this.shell.setLayout(new FillLayout());

        this.shell.setText(SquashFSGlobals.PROJECT_NAME + " GUI");

        createMenu();

        this.content = new ContentComposite(this.shell, 0);

        onNew();

        this.shell.open();
        while (!this.shell.isDisposed()) {
            try {
                if (!this.display.readAndDispatch()) {
                    this.display.sleep();
                }
            } catch (Throwable e) {
                // TODO catch and display error.
                log.error("error", e);
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
    }

    /**
     * 
     */
    protected void onOpen() {
        // TODO check that the last loaded was saved.

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
                } catch (Exception e2) {
                    MessageBox mb = new MessageBox(this.shell, SWT.OK
                            | SWT.ICON_ERROR);
                    mb.setText("Error Loading");
                    mb.setMessage("Error Loading\n" + e2.getMessage());
                }
            } finally {
                this.shell.setCursor(this.CURSOR_ARROW);
            }
        }
    }

    /**
     * creates a new file. 
     */
    protected void onNew() {
        // TODO check that the last loaded was saved.

        this.manifest = new Manifest();
        this.manifest.setRoot(new Directory(null, 0, 0, 0, 0));

        this.content.loadManifest(this.manifest);
    }

    /**
     * main entry point.
     * @param args the command line arguments.
     */
    public static void main(String args[]) {
        new JSquashFSGUI(args);
    }
}
