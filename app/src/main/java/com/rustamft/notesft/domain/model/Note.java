package com.rustamft.notesft.domain.model;

public abstract class Note {

    public abstract void setText(String text);

    /**
     * Returns a boolean indicating whether this file can be found.
     *
     * @return true if this file exists, false otherwise.
     */
    public abstract boolean exists();

    /**
     * Returns a Uri as a String for the underlying document represented by this file.
     *
     * @return a String containing the Uri.
     */
    public abstract String path();

    /**
     * Returns the length of this file in bytes.
     * Returns 0 if the file does not exist, or if the length is unknown.
     * The result for a directory is not defined.
     *
     * @return the number of bytes in this file.
     */
    public abstract long length();

    /**
     * Returns the time when this file was last modified
     *
     * @return the time when this file was last modified.
     */
    public abstract long lastModified();

    /**
     * Returns the file directory.
     *
     * @return a String with the directory path the file stored ib.
     */
    public abstract String getWorkingDir();

    /**
     * Returns the file name.
     *
     * @return a String with the name stored in the note instance.
     */
    public abstract String getName();

    /**
     * Deletes this file.
     *
     * @return true if this file was deleted, false otherwise.
     */
    protected abstract boolean delete();

    /**
     * Change the name of an existing file.
     *
     * @param newName updated name for document.
     * @return true if this file was renamed, false otherwise.
     */
    public abstract boolean rename(String newName); // TODO: make protected

    /**
     * Writes a given text to an existing file.
     *
     * @param text a text to save to the file.
     * @return true if the text has been saved, false otherwise.
     */
    public abstract boolean save(String text); // TODO: make protected

    /**
     * Reads a text this file contains.
     *
     * @return a String with the file text.
     */
    public abstract String buildStringFromContent(); // TODO: make protected
}
