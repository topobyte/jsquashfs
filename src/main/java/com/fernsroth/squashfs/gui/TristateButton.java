/**
 * Created Jun 22, 2006
 */
package com.fernsroth.squashfs.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:klaus.wenger@u14n.com?subject=com.u14n.ui.controls.TristateButton">Klaus Wenger</a>
 */
public class TristateButton extends Button {
    /**
     * @param parent
     * @param style
     */
    public TristateButton(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * @see org.eclipse.swt.widgets.Widget#checkSubclass()
     */
    @Override
    protected void checkSubclass() {
        // Forcing permitted subclassing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkWidget() {
        super.checkWidget();
        int bits = OS.GetWindowLong(this.handle, OS.GWL_STYLE);
        //System.out.println(getClass().getName() + ".checkWidget() (bits & OS.BS_CHECKBOX)=" + (bits & OS.BS_CHECKBOX)); //$NON-NLS-1$
        //System.out.println(getClass().getName() + ".checkWidget() (bits & OS.WS_TABSTOP)=" + (bits & OS.WS_TABSTOP)); //$NON-NLS-1$
        //System.out.println(getClass().getName() + ".checkWidget() (bits & BS_3STATE)=" + (bits & BS_3STATE)); //$NON-NLS-1$
        //System.out.println(getClass().getName() + ".checkWidget() (bits & BS_AUTO3STATE)=" + (bits & BS_AUTO3STATE)); //$NON-NLS-1$
        bits |= BS_3STATE;
        bits &= ~OS.BS_CHECKBOX;
        OS.SetWindowLong(this.handle, OS.GWL_STYLE, bits);
    }

    /**
     * @param grayed
     */
    public void setGrayed(boolean grayed) {
        //System.out.println(getClass().getName()
        //        + ".setGrayed() grayed=" + grayed); //$NON-NLS-1$
        checkWidget();
        if ((getStyle() & SWT.CHECK) == 0)
            return;
        int flags = grayed ? BST_INDETERMINATE
                : getSelection() ? OS.BST_CHECKED : OS.BST_UNCHECKED;
        //System.out
        //        .println(getClass().getName() + ".setGrayed() flags=" + flags); //$NON-NLS-1$
        /*
         * Feature in Windows. When BM_SETCHECK is used
         * to set the checked state of a radio or check
         * button, it sets the WM_TABSTOP style. This
         * is undocumented and unwanted. The fix is
         * to save and restore the window style bits.
         */
        int bits = OS.GetWindowLong(this.handle, OS.GWL_STYLE);
        OS.SendMessage(this.handle, OS.BM_SETCHECK, flags, 0);
        OS.SetWindowLong(this.handle, OS.GWL_STYLE, bits);
    }

    /**
     * @return <code>true</code>, if greyed, else <code>false</code>
     */
    public boolean getGrayed() {
        checkWidget();
        if ((getStyle() & SWT.CHECK) == 0)
            return false;
        int state = OS.SendMessage(this.handle, OS.BM_GETCHECK, 0, 0);
        //System.out
        //        .println(getClass().getName() + ".getGrayed() state=" + state); //$NON-NLS-1$
        return (state & BST_INDETERMINATE) != 0;
    }

    /**
     * 
     */
    private static final int BS_3STATE = 0x00000005;

    /**
     * 
     */
    private static final short BST_INDETERMINATE = 0x0002;
}
