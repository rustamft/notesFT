package com.rustamft.notesft.database;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.List;

public class NotesSharedPrefs implements SharedPrefs {
    private final Application mApplication;
    private final SharedPreferences mSharedPrefs;
    private final String NIGHT_MODE = "night_mode";
    private final String WORKING_DIR_KEY = "working_dir";

    public NotesSharedPrefs(Application application) {
        mApplication = application;
        final String SHARED_PREF_FILE = "com.rustamft.notesft.shared_preferences";
        mSharedPrefs = application.getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
    }

    public boolean hasPermission() {
        // Get working dir
        String workingDir = getWorkingDir();
        // Check if there is saved workingDir in shared preferences
        if (workingDir == null) {
            return false;
        }
        // Check permission
        List<UriPermission> permissionsList =
                mApplication.getContentResolver().getPersistedUriPermissions();

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

    public void setNightMode(int mode) {
        mSharedPrefs
                .edit()
                .putInt(NIGHT_MODE, mode)
                .apply();
    }

    public int getNightMode() {
        return mSharedPrefs.getInt(NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setWorkingDir(Intent resultData) {
        // Get URI from result.
        Uri directoryUri = resultData.getData();
        // Persist the permission.
        final int flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        mApplication.getContentResolver().takePersistableUriPermission(directoryUri, flags);
        // Save to SharedPrefs.
        String workingDir = directoryUri.toString();
        mSharedPrefs
                .edit()
                .putString(WORKING_DIR_KEY, workingDir)
                .apply();
    }

    public String getWorkingDir() {
        return mSharedPrefs.getString(WORKING_DIR_KEY, null);
    }
}
