/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.fernsroth.squashfs.SquashFSUtils;
import com.fernsroth.squashfs.model.BaseFile;
import com.fernsroth.squashfs.model.SFSSourceFile;
import com.fernsroth.squashfs.model.squashfs.stat;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public class PropertiesGeneralTab implements PropertiesDialog.PropertyTab {
    /**
     * 
     */
    private static final String MULTIPLE_FILES_SELECTED = "[multiple files selected]";

    /**
     * uid field.
     */
    private Text txtUid;

    /**
     * guid field.
     */
    private Text txtGuid;

    /**
     * owner mode.
     */
    private ModeComposite modeOwner;

    /**
     * group mode.
     */
    private ModeComposite modeGroup;

    /**
     * other mode.
     */
    private ModeComposite modeOther;

    /**
     * @param tabFolder
     * @param files 
     */
    public PropertiesGeneralTab(TabFolder tabFolder, List<BaseFile> files) {
        TabItem generalTab = new TabItem(tabFolder, 0);
        generalTab.setText("General");
        Composite composite = new Composite(tabFolder, 0);
        generalTab.setControl(composite);
        FormLayout formLayout = new FormLayout();
        formLayout.marginLeft = 8;
        formLayout.marginTop = 8;
        formLayout.marginRight = 8;
        formLayout.marginBottom = 8;
        formLayout.spacing = 8;
        composite.setLayout(formLayout);

        FormData formData;
        Control top;

        // name
        Label lblName = new Label(composite, 0);
        lblName.setText("Name:");
        formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(0);
        lblName.setLayoutData(formData);

        Text txtName = new Text(composite, SWT.READ_ONLY);
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (BaseFile bf : files) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(bf.getName());
            first = false;
        }
        txtName.setText(sb.toString());
        formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(20);
        formData.right = new FormAttachment(100);
        txtName.setLayoutData(formData);
        top = txtName;

        // source
        if (files.get(0) instanceof SFSSourceFile) {
            Label lblSource = new Label(composite, 0);
            lblSource.setText("Source:");
            formData = new FormData();
            formData.top = new FormAttachment(top);
            formData.left = new FormAttachment(0);
            lblSource.setLayoutData(formData);

            Text txtSource = new Text(composite, SWT.BORDER);
            if (files.size() == 1) {
                SFSSourceFile sourceFile = (SFSSourceFile) files.get(0);
                if (sourceFile.getSourceFile() != null) {
                    txtSource.setText(sourceFile.getSourceFile()
                            .getAbsolutePath());
                }
            } else {
                txtSource.setText(MULTIPLE_FILES_SELECTED);
            }
            formData = new FormData();
            formData.top = new FormAttachment(top);
            formData.left = new FormAttachment(20);
            formData.right = new FormAttachment(100);
            txtSource.setLayoutData(formData);
            top = txtSource;
        }

        // GUID
        {
            Label lblGuid = new Label(composite, 0);
            lblGuid.setText("GUID:");
            formData = new FormData();
            formData.top = new FormAttachment(top);
            formData.left = new FormAttachment(0);
            lblGuid.setLayoutData(formData);

            this.txtGuid = new Text(composite, SWT.BORDER);
            if (files.size() == 1) {
                this.txtGuid.setText(Long.toString(files.get(0).getGuid()));
            } else {
                Long guid = new Long(files.get(0).getGuid());
                for (BaseFile bf : files) {
                    if (bf.getGuid() != guid) {
                        guid = null;
                        break;
                    }
                }
                if (guid == null) {
                    this.txtGuid.setText(MULTIPLE_FILES_SELECTED);
                } else {
                    this.txtGuid.setText(guid.toString());
                }
            }
            formData = new FormData();
            formData.top = new FormAttachment(top);
            formData.left = new FormAttachment(20);
            formData.right = new FormAttachment(100);
            this.txtGuid.setLayoutData(formData);
            top = this.txtGuid;
        }

        // UID
        {
            Label lblUid = new Label(composite, 0);
            lblUid.setText("UID:");
            formData = new FormData();
            formData.top = new FormAttachment(top);
            formData.left = new FormAttachment(0);
            lblUid.setLayoutData(formData);

            this.txtUid = new Text(composite, SWT.BORDER);
            if (files.size() == 1) {
                this.txtUid.setText(Long.toString(files.get(0).getUid()));
            } else {
                Long uid = new Long(files.get(0).getUid());
                for (BaseFile bf : files) {
                    if (bf.getUid() != uid) {
                        uid = null;
                        break;
                    }
                }
                if (uid == null) {
                    this.txtUid.setText(MULTIPLE_FILES_SELECTED);
                } else {
                    this.txtUid.setText(uid.toString());
                }
            }
            formData = new FormData();
            formData.top = new FormAttachment(top);
            formData.left = new FormAttachment(20);
            formData.right = new FormAttachment(100);
            this.txtUid.setLayoutData(formData);
            top = this.txtUid;
        }

        // MTime
        {
            Label lblMTime = new Label(composite, 0);
            lblMTime.setText("MTime:");
            formData = new FormData();
            formData.top = new FormAttachment(top);
            formData.left = new FormAttachment(0);
            lblMTime.setLayoutData(formData);

            Text txtMTime = new Text(composite, SWT.BORDER);
            if (files.size() == 1) {
                txtMTime.setText(GUIUtils.formatMTime(files.get(0).getMTime()));
            } else {
                txtMTime.setText(MULTIPLE_FILES_SELECTED);
            }
            formData = new FormData();
            formData.top = new FormAttachment(top);
            formData.left = new FormAttachment(20);
            formData.right = new FormAttachment(100);
            txtMTime.setLayoutData(formData);
            top = txtMTime;
        }

        {
            Group mode = new Group(composite, SWT.SHADOW_ETCHED_IN);
            mode.setText("Mode");
            formData = new FormData();
            formData.top = new FormAttachment(top);
            formData.left = new FormAttachment(0);
            formData.right = new FormAttachment(100);
            mode.setLayoutData(formData);
            FormLayout modeFormLayout = new FormLayout();
            modeFormLayout.marginLeft = 8;
            modeFormLayout.marginTop = 8;
            modeFormLayout.marginRight = 8;
            modeFormLayout.marginBottom = 8;
            modeFormLayout.spacing = 8;
            mode.setLayout(modeFormLayout);

            this.modeOwner = new ModeComposite(mode, 0, "Owner");
            formData = new FormData();
            formData.left = new FormAttachment(0);
            formData.top = new FormAttachment(0);
            formData.bottom = new FormAttachment(100);
            this.modeOwner.setLayoutData(formData);
            if (files.size() == 1) {
                this.modeOwner.setMode(SquashFSUtils.getOwnerMode(files.get(0)
                        .getMode()));
            } else {
                int zmode = 0;
                int zgrayed = 0;
                for (BaseFile bf : files) {
                    int m = SquashFSUtils.getOwnerMode(bf.getMode());
                    zmode |= m;
                    zgrayed |= (m ^ zmode) & 0x7;
                }
                this.modeOwner.setMode(zmode);
                this.modeOwner.setGrayed(zgrayed);
            }

            this.modeGroup = new ModeComposite(mode, 0, "Group");
            formData = new FormData();
            formData.left = new FormAttachment(this.modeOwner);
            formData.top = new FormAttachment(0);
            formData.bottom = new FormAttachment(100);
            this.modeGroup.setLayoutData(formData);
            if (files.size() == 1) {
                this.modeGroup.setMode(SquashFSUtils.getGroupMode(files.get(0)
                        .getMode()));
            } else {
                int zmode = 0;
                int zgrayed = 0;
                for (BaseFile bf : files) {
                    int m = SquashFSUtils.getGroupMode(bf.getMode());
                    zmode |= m;
                    zgrayed |= (m ^ zmode) & 0x7;
                }
                this.modeGroup.setMode(zmode);
                this.modeGroup.setGrayed(zgrayed);
            }

            this.modeOther = new ModeComposite(mode, 0, "Other");
            formData = new FormData();
            formData.left = new FormAttachment(this.modeGroup);
            formData.top = new FormAttachment(0);
            formData.bottom = new FormAttachment(100);
            formData.right = new FormAttachment(100);
            this.modeOther.setLayoutData(formData);
            if (files.size() == 1) {
                this.modeOther.setMode(SquashFSUtils.getOtherMode(files.get(0)
                        .getMode()));
            } else {
                int zmode = 0;
                int zgrayed = 0;
                for (BaseFile bf : files) {
                    int m = SquashFSUtils.getOtherMode(bf.getMode());
                    zmode |= m;
                    zgrayed |= (m ^ zmode) & 0x7;
                }
                this.modeOther.setMode(zmode);
                this.modeOther.setGrayed(zgrayed);
            }

            top = mode;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void validate(List<BaseFile> files) throws ValidationException {
        getGuid();
        getUid();
    }

    /**
     * {@inheritDoc}
     */
    public void apply(List<BaseFile> files) {
        try {
            Long guid = getGuid();
            Long uid = getUid();

            for (BaseFile bf : files) {
                // apply guid
                if (guid != null) {
                    bf.setGuid(guid);
                }

                // apply uid
                if (uid != null) {
                    bf.setUid(uid);
                }

                // apply mtime
                // TODO apply mtime

                // apply permission
                bf.setMode(getMode(bf.getMode()));
            }
        } catch (ValidationException e) {
            // do nothing this should be handled by validate.
        }
    }

    /**
     * gets the selected mode.
     * @param mode the initial mode of the file.
     * @return the modified mode.
     */
    private int getMode(int mode) {
        int zmode = SquashFSUtils.getMode(this.modeOwner.getMode(),
                this.modeGroup.getMode(), this.modeOther.getMode());
        int zgrayed = SquashFSUtils.getMode(this.modeOwner.getGrayed(),
                this.modeGroup.getGrayed(), this.modeOther.getGrayed());
        return (zmode | (mode & zgrayed));
    }

    /**
     * @return the uid if set. null, if not set.
     * @throws ValidationException 
     */
    private Long getUid() throws ValidationException {
        if (this.txtUid.getText().equals(MULTIPLE_FILES_SELECTED)) {
            return null;
        }
        try {
            return Long.parseLong(this.txtUid.getText());
        } catch (NumberFormatException e) {
            throw new ValidationException("UID " + e.getMessage());
        }
    }

    /**
     * @return the guid if set. null, if not set.
     * @throws ValidationException 
     */
    private Long getGuid() throws ValidationException {
        if (this.txtGuid.getText().equals(MULTIPLE_FILES_SELECTED)) {
            return null;
        }
        try {
            return Long.parseLong(this.txtGuid.getText());
        } catch (NumberFormatException e) {
            throw new ValidationException("GUID " + e.getMessage());
        }
    }

    /**
     * composite for mode data.
     */
    protected class ModeComposite extends Composite {
        /**
         * the read checkbox.
         */
        private Button buttonRead;

        /**
         * the write checkbox.
         */
        private Button buttonWrite;

        /**
         * the execute checkbox.
         */
        private Button buttonExecute;

        /**
         * constructor.
         * @param parent the parent.
         * @param style the style.
         * @param modeName the mode name (User, Group, Other).
         */
        public ModeComposite(Composite parent, int style, String modeName) {
            super(parent, style);
            setLayout(new GridLayout());
            Label lbl = new Label(this, 0);
            lbl.setText(modeName);

            this.buttonRead = new Button(this, SWT.CHECK);
            this.buttonRead.setText("Read");

            this.buttonWrite = new Button(this, SWT.CHECK);
            this.buttonWrite.setText("Write");

            this.buttonExecute = new Button(this, SWT.CHECK);
            this.buttonExecute.setText("Execute");

            pack();
        }

        /**
         * @param mode
         */
        public void setMode(int mode) {
            this.buttonRead.setSelection((mode & stat.S_IR) != 0);
            this.buttonWrite.setSelection((mode & stat.S_IW) != 0);
            this.buttonExecute.setSelection((mode & stat.S_IX) != 0);
        }

        /**
         * gets the mode.
         * @return the mode.
         */
        public int getMode() {
            int mode = 0;
            if (!this.buttonRead.getGrayed() && this.buttonRead.getSelection()) {
                mode |= stat.S_IR;
            }
            if (!this.buttonWrite.getGrayed()
                    && this.buttonWrite.getSelection()) {
                mode |= stat.S_IW;
            }
            if (!this.buttonExecute.getGrayed()
                    && this.buttonExecute.getSelection()) {
                mode |= stat.S_IX;
            }
            return mode;
        }

        /**
         * @param mode
         */
        public void setGrayed(int mode) {
            this.buttonRead.setGrayed((mode & stat.S_IR) != 0);
            this.buttonWrite.setGrayed((mode & stat.S_IW) != 0);
            this.buttonExecute.setGrayed((mode & stat.S_IX) != 0);
        }

        /**
         * gets the grayed.
         * @return the grayed.
         */
        public int getGrayed() {
            int mode = 0;
            if (this.buttonRead.getGrayed()) {
                mode |= stat.S_IR;
            }
            if (this.buttonWrite.getGrayed()) {
                mode |= stat.S_IW;
            }
            if (this.buttonExecute.getGrayed()) {
                mode |= stat.S_IX;
            }
            return mode;
        }
    }
}
