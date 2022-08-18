package com.rustamft.notesft.database;

import android.content.Intent;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

public interface SharedPrefs {

    @Module
    @InstallIn(SingletonComponent.class)
    abstract class SharedPrefsModule {
        @Binds
        @Singleton
        abstract SharedPrefs bindSharedPrefs(NotesSharedPrefs sharedPrefs);
    }

    /**
     * Checks if the app has read/write permission by iterating through the app's permission list.
     *
     * @return true if the permission is granted, false otherwise.
     */
    boolean hasPermission();

    /**
     * Sets a remembered state of the night mode for the app.
     *
     * @param mode the mode to be remembered.
     */
    void setNightMode(int mode);

    /**
     * Returns a night mode state saved.
     *
     * @return a night mode state saved, otherwise system default.
     */
    int getNightMode();

    /**
     * Writes the working directory to the app's SharedPreferences.
     *
     * @param resultData a data result from a folder chooser intent.
     */
    void setWorkingDir(Intent resultData);

    /**
     * Gets a working directory path stored in SharedPreferences.
     *
     * @return a String with the working directory path or null if there's none stored.
     */
    String getWorkingDir();
}
