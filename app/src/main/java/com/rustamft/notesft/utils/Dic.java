package com.rustamft.notesft.utils;

import android.app.Application;

import com.rustamft.notesft.database.NotesRepository;
import com.rustamft.notesft.database.NotesSharedPrefs;
import com.rustamft.notesft.database.Repository;
import com.rustamft.notesft.database.SharedPrefs;
import com.rustamft.notesft.models.File;
import com.rustamft.notesft.models.NoteFile;

/**
 * Dependency Injection Container that provides instances of the vital parts of the app.
 */
public class Dic {
    private final Application mApplication;
    private static Repository REPOSITORY;
    private static SharedPrefs SHARED_PREFS;

    public Dic(Application application) {
        mApplication = application;
        if (REPOSITORY == null) {
            REPOSITORY = new NotesRepository(mApplication);
        }
        if (SHARED_PREFS == null) {
            SHARED_PREFS = new NotesSharedPrefs(mApplication);
        }
    }

    /**
     * Returns the Repository instance.
     *
     * @return the saved instance of Repository or a new one.
     */
    public Repository getRepository() {
        return REPOSITORY;
    }

    /**
     * Returns the SharedPrefs instance.
     *
     * @return the saved instance of SharedPrefs or a new one.
     */
    public SharedPrefs getSharedPrefs() {
        return SHARED_PREFS;
    }

    /**
     * Returns a file instance if the given name is correct.
     *
     * @param name the name of the file.
     * @return an instance of a note file.
     */
    public File getFileInstance(String name) {
        // Check if the name is valid: has at least one letter or digit.
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if (Character.isLetterOrDigit(c)) { // If the name is valid
                String workingDir = getSharedPrefs().getWorkingDir();
                return new NoteFile(mApplication, workingDir, name);
            }
        }
        return null; // If the name is invalid
    }
}