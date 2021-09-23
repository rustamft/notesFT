package com.rustamft.notesft.utils;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;

import java.util.List;

public class AppSharedPreferences {
    private final Application mApplication;
    private final String SHARED_PREF_FILE = "com.rustamft.notesft.shared_preferences";
    private final String WORKING_DIR_KEY = "working_dir";

    public AppSharedPreferences(Application application) {
        mApplication = application;
    }

    /**
     * Checks if the app has read/write permission by iterating through the app's permission list.
     * @return true if the permission is granted, false otherwise.
     */
    public boolean hasPermission() {
        // Get working dir
        String workingDir = getWorkingDir();
        // Check if there is saved workingDir in shared preferences
        if (workingDir == null) {
            return false;
        }
        // Check permission
        List<UriPermission> permissionsList = mApplication.getContentResolver()
                .getPersistedUriPermissions();

        if (permissionsList.size() != 0) {
            for (UriPermission permission : permissionsList) {
                if (permission.getUri().toString().equals(workingDir)) {
                    if (permission.isWritePermission() && permission.isReadPermission()) {
                        return true;
                    }
                }
            }
        }
        // If there is no permission
        return false;
    }

    /**
     * Writes the working directory to the app's SharedPreferences.
     * @param resultData a data result from a folder chooser intent.
     */
    @SuppressLint("WrongConstant")
    public void setWorkingDir(Intent resultData) {
        Uri directoryUri = resultData.getData();
        final int flags = resultData.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        mApplication.getContentResolver().takePersistableUriPermission(directoryUri, flags);

        String workingDir = directoryUri.toString();
        SharedPreferences sharedPreferences = mApplication
                .getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
        sharedPreferences
                .edit()
                .putString(WORKING_DIR_KEY, workingDir)
                .apply();
    }

    /**
     * Gets a working directory path stored in SharedPreferences.
     * @return a String with the working directory path or null if there's none stored.
     */
    public String getWorkingDir() {
        SharedPreferences sharedPreferences = mApplication
                .getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
        return sharedPreferences.getString(WORKING_DIR_KEY, null);
    }
}
