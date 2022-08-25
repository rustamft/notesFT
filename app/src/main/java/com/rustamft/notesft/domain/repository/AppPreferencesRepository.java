package com.rustamft.notesft.domain.repository;

import android.content.Intent;

public interface AppPreferencesRepository {

    /**
     * Checks if the app has read/write permission by iterating through the app's permission list.
     *
     * @return true if the permission is granted, false otherwise.
     */
    boolean hasWorkingDirPermission();

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
     * Gets a working directory getPath stored in SharedPreferences.
     *
     * @return a String with the working directory getPath or null if there's none stored.
     */
    String getWorkingDir();
}
