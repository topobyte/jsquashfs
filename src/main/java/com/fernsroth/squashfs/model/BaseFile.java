/**
 * 
 */
package com.fernsroth.squashfs.model;

/**
 * base class for files.
 * @author Joseph M. Ferner (Near Infinity Corporation)
 */
public abstract class BaseFile {
    /**
     * name of the file.
     */
    private String name;

    /**
     * 
     */
    private int mode;

    /**
     * 
     */
    private long mTime;

    /**
     * 
     */
    private long guid;

    /**
     * 
     */
    private long uid;

    /**
     * constructor. 
     * @param name the name of the file.
     * @param mode the mode.
     * @param mTime the modify time.
     * @param guid the group id.
     * @param uid the user id.
     */
    public BaseFile(String name, int mode, long mTime, long guid, long uid) {
        this.name = name;
        this.mode = mode;
        this.mTime = mTime;
        this.guid = guid;
        this.uid = uid;
    }

    /**
     * @return the guid
     */
    public long getGuid() {
        return this.guid;
    }

    /**
     * @return the mTime
     */
    public long getMTime() {
        return this.mTime;
    }

    /**
     * @return the uid
     */
    public long getUid() {
        return this.uid;
    }

    /**
     * @return the mode
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

}
