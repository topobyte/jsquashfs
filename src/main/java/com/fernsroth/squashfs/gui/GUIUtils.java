/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fernsroth.squashfs.SquashFSUtils;

/**
 * 
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public final class GUIUtils {
    /**
     * hidden constructor. 
     */
    private GUIUtils() {
        // hide it.
    }

    /**
     * formats a mtime for display.
     * @param mtime the mtime to format.
     * @return the string.
     */
    public static String formatMTime(long mtime) {
        return formatDate(SquashFSUtils.getDateFromMTime(mtime));
    }

    /**
     * formats a date for display.
     * @param date the date to format.
     * @return the string.
     */
    public static String formatDate(Date date) {
        return new SimpleDateFormat().format(date);
    }
}
