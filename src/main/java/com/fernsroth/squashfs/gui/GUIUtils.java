/**
 * 
 */
package com.fernsroth.squashfs.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

    /**
     * convert an exception into a string.
     * @param e the exception to convert.
     * @return the exception as a string including stack trace.
     */
    public static String exceptionToString(Throwable e) {
        StringBuffer sb = new StringBuffer();
        sb.append(e.getMessage());
        sb.append("\n");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        ps.close();
        sb.append(baos.toString());
        return sb.toString();
    }
}
