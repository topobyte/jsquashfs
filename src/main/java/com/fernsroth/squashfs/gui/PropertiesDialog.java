/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;

import com.fernsroth.squashfs.model.BaseFile;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class PropertiesDialog extends Dialog {

    /**
     * the result of the dialog.
     */
    private boolean result;

    /**
     * the shell.
     */
    private Shell shell;

    /**
     * the files being worked on.
     */
    private List<BaseFile> files;

    /**
     * list of property tabs.
     */
    private List<PropertyTab> propertyTabs = new ArrayList<PropertyTab>();

    /**
     * constructor.
     * @param parent the parent shell.
     * @param style the style.
     * @param files the files to show properties for.
     */
    public PropertiesDialog(Shell parent, int style, List<BaseFile> files) {
        super(parent, style);
        this.files = files;
        this.shell = new Shell(parent, getStyle() | SWT.DIALOG_TRIM
                | SWT.APPLICATION_MODAL);
        this.shell.setText("Properties");
        FormLayout formLayout = new FormLayout();
        formLayout.marginLeft = 8;
        formLayout.marginTop = 8;
        formLayout.marginRight = 8;
        formLayout.marginBottom = 8;
        formLayout.spacing = 8;
        this.shell.setLayout(formLayout);
        this.result = false;

        FormData formData;
        Button buttonCancel = new Button(this.shell, SWT.PUSH);
        buttonCancel.setText("Cancel");
        formData = new FormData();
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(100);
        formData.width = 72;
        buttonCancel.setLayoutData(formData);
        buttonCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onCancel();
            }
        });

        Button buttonOK = new Button(this.shell, SWT.PUSH);
        buttonOK.setText("OK");
        formData = new FormData();
        formData.right = new FormAttachment(buttonCancel);
        formData.bottom = new FormAttachment(100);
        formData.width = 72;
        buttonOK.setLayoutData(formData);
        buttonOK.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onOK();
            }
        });

        TabFolder tabFolder = new TabFolder(this.shell, SWT.TOP);
        formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(buttonOK);
        tabFolder.setLayoutData(formData);
        this.propertyTabs.add(new PropertiesGeneralTab(tabFolder, files));

        buttonCancel.setFocus();

        this.shell.setSize(400, 550);
    }

    /**
     * exit.
     */
    protected void onCancel() {
        this.result = false;
        this.shell.dispose();
    }

    /**
     * save changes and exit.
     */
    protected void onOK() {
        try {
            for (PropertyTab pt : this.propertyTabs) {
                pt.validate(this.files);
            }
            for (PropertyTab pt : this.propertyTabs) {
                pt.apply(this.files);
            }
            this.result = true;
            this.shell.dispose();
        } catch (ValidationException e) {
            MessageBox mb = new MessageBox(this.getParent().getShell(), SWT.OK
                    | SWT.ICON_ERROR);
            mb.setText("Error Validating Input");
            mb.setMessage("Error Validating Input\n" + e.getMessage());
            mb.open();
        }
    }

    /**
     * open the dialog.
     * @return true, if something was changes. false, if nothing was changed.
     */
    public boolean open() {
        this.shell.open();
        Display display = getParent().getDisplay();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return this.result;
    }

    /**
     * property tab interface.
     */
    public static interface PropertyTab {

        /**
         * applies changes.
         * @param files the files to apply changes to.
         */
        void apply(List<BaseFile> files);

        /**
         * validates the changes.
         * @param files the files to check changes to.
         * @throws ValidationException 
         */
        void validate(List<BaseFile> files) throws ValidationException;
    }
}
