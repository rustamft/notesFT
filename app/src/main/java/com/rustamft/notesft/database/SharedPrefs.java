package com.rustamft.notesft.database;

import android.content.Intent;

public interface SharedPrefs {

    /**
     * Checks if the app has read/write permission by iterating through the app's permission list.
     * @return true if the permission is granted, false otherwise.
     */
    boolean hasPermission();

    /**
     * Writes the working directory to the app's SharedPreferences.
     * @param resultData a data result from a folder chooser intent.
     */
    void setWorkingDir(Intent resultData);

    /**
     * Gets a working directory path stored in SharedPreferences.
     * @return a String with the working directory path or null if there's none stored.
     */
    String getWorkingDir();
}