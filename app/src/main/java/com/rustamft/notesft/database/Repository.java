package com.rustamft.notesft.database;

import androidx.lifecycle.MutableLiveData;

import com.rustamft.notesft.models.File;

import java.util.List;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

public interface Repository {

    @Module
    @InstallIn(SingletonComponent.class)
    abstract class RepositoryModule {
        @Binds
        @Singleton
        abstract Repository bindRepository(NotesRepository repository);
    }

    /**
     * Returns a name of a given file.
     *
     * @param file the note file instance.
     * @return a String with the file name.
     */
    String getFileName(File file);

    /**
     * Returns a date the file was last modified.
     *
     * @param file the note file instance.
     * @return a String with the file last modified formatted date.
     */
    String lastModifiedAsString(File file);

    /**
     * Creates a file with the given name.
     *
     * @param file the note file instance.
     * @return true if the file has been created successfully, false otherwise.
     */
    boolean createFile(File file);

    /**
     * Reads the working directory contents and builds a files list.
     *
     * @param liveDataFilesList a files list LiveData to update with the files list.
     */
    void updateFilesList(String workingDir, MutableLiveData<List<String>> liveDataFilesList);

    /**
     * Deletes a given file.
     *
     * @param file              the note file instance.
     * @param liveDataFilesList a files list LiveData to update.
     */
    void deleteFile(File file, MutableLiveData<List<String>> liveDataFilesList);

    /**
     * Renames a given file.
     *
     * @param file                   the file instance.
     * @param newName                a new name for the file.
     * @param liveDataActionBarTitle an ActionBar title LiveData to update with the new name.
     */
    void renameFile(File file, String newName, MutableLiveData<String> liveDataActionBarTitle);

    /**
     * Saves the given text to the file.
     *
     * @param file the note file instance.
     * @param text a text to save to the current note.
     */
    void saveFile(File file, String text);


}
