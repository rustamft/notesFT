package com.rustamft.notesft.database;

import androidx.lifecycle.MutableLiveData;

import com.rustamft.notesft.models.File;

public interface Repository {

    /**
     * Returns a name of a given file.
     * @param file the note file instance.
     * @return a String with the file name.
     */
    String getFileName(File file);

    /**
     * Reads a text this file contains.
     * @param file the note file instance.
     * @return a String with the file text.
     */
    String getFileText(File file);

    /**
     * Returns the length of this file in bytes.
     * Returns 0 if the file does not exist, or if the length is unknown.
     * The result for a directory is not defined.
     * @param file the note file instance.
     * @return the number of bytes in this file.
     */
    long getFileLength(File file);

    /**
     * Returns a date the file was last modified.
     * @param file the note file instance.
     * @return a String with the file last modified formatted date.
     */
    String lastModified(File file);

    /**
     * Creates a file with the given name.
     * @param file the note file instance.
     * @return true if the file has been created successfully, false otherwise.
     */
    boolean createNewFile(File file);

    /**
     * Reads the working directory contents and builds a files list.
     * @param liveDataFilesList a files list LiveData to update with the files list.
     */
    void updateFilesList(String workingDir, MutableLiveData<String[]> liveDataFilesList);

    /**
     * Deletes a given file.
     * @param file the note file instance.
     * @param liveDataFilesList a files list LiveData to update.
     */
    void deleteFile(File file, MutableLiveData<String[]> liveDataFilesList);

    /**
     * Renames a given file.
     * @param file the file instance.
     * @param newName a new name for the file.
     * @param liveDataActionBarTitle an ActionBar title LiveData to update with the new name.
     */
    void renameFile(File file, String newName, MutableLiveData<String> liveDataActionBarTitle);

    /**
     * Saves the given text to the file.
     * @param file the note file instance.
     * @param text a text to save to the current note.
     */
    void saveTextToFile(File file, String text);


}
