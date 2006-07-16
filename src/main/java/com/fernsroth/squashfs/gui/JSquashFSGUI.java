/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.fernsroth.squashfs.SquashFSGlobals;
import com.fernsroth.squashfs.SquashFSManifest;
import com.fernsroth.squashfs.exception.SquashFSException;
import com.fernsroth.squashfs.model.Directory;
import com.fernsroth.squashfs.model.Manifest;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class JSquashFSGUI {
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
     * creates a new file. 
     */
    protected void onNew() {
        // TODO check that the last loaded was saved.

        this.manifest = new Manifest();
        this.manifest.setRoot(new Directory(null, 0, 0, 0, 0));

        try {
            File sourceFile = new File("C:/temp/slax/manifest.xml");
            File sourceDir = new File("C:/temp/slax/");
            this.manifest = SquashFSManifest.load(new FileInputStream(
                    sourceFile), sourceDir);
        } catch (SquashFSException e) {
            // TODO remove
        } catch (FileNotFoundException e) {
            // TODO remove
        } catch (IOException e) {
            // TODO remove
        }

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
