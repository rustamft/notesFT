package com.rustamft.notesft.utils;

import android.app.Application;

import com.rustamft.notesft.models.File;
import com.rustamft.notesft.models.Note;

public class FileFactory {
    Application mApplication;
    String mWorkingDir;

    public FileFactory(Application application, String workingDir) {
        mApplication = application;
        mWorkingDir = workingDir;
    }

    /**
     * Returns a file instance if the given name is correct.
     * @param name the name of the file.
     * @return an instance of a note file.
     */
    public File getFileInstance(String name) {
        // Check if the name is valid: has at least one letter or digit
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if (Character.isLetterOrDigit(c)) { // If the name is valid
                return new Note(mApplication, mWorkingDir, name); // The cycle stopped
            }
        }
        return null; // If the name is invalid
    }
}