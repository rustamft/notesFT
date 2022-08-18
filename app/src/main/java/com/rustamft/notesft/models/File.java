package com.rustamft.notesft.models;

public interface File {

    /**
     * Returns a boolean indicating whether this file can be found.
     *
     * @return true if this file exists, false otherwise.
     */
    boolean exists();

    /**
     * Returns a Uri as a String for the underlying document represented by this file.
     *
     * @return a String containing the Uri.
     */
    String path();

    /**
     * Returns the length of this file in bytes.
     * Returns 0 if the file does not exist, or if the length is unknown.
     * The result for a directory is not defined.
     *
     * @return the number of bytes in this file.
     */
    long length();

    /**
     * Returns the time when this file was last modified
     *
     * @return the time when this file was last modified.
     */
    long lastModified();

    /**
     * Returns the file directory.
     *
     * @return a String with the directory path the file stored ib.
     */
    String getWorkingDir();

    /**
     * Returns the file name.
     *
     * @return a String with the name stored in the note instance.
     */
    String getName();

    /**
     * Creates a new file in the working directory.
     *
     * @return true if the file was created, false otherwise.
     */
    boolean create();

    /**
     * Deletes this file.
     *
     * @return true if this file was deleted, false otherwise.
     */
    boolean delete();

    /**
     * Change the name of an existing file.
     *
     * @param newName updated name for document.
     * @return true if this file was renamed, false otherwise.
     */
    boolean rename(String newName);

    /**
     * Writes a given text to an existing file.
     *
     * @param text a text to save to the file.
     * @return true if the text has been saved, false otherwise.
     */
    boolean save(String text);

    /**
     * Reads a text this file contains.
     *
     * @return a String with the file text.
     */
    String buildStringFromContent();
}
