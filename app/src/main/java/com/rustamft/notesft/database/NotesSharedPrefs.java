package com.rustamft.notesft.database;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;

import androidx.appcompat.app.AppCompatDelegate;

import com.rustamft.notesft.utils.Constants;

import java.util.List;

import javax.inject.Inject;

public class NotesSharedPrefs implements SharedPrefs {

    private final Application application;
    private final SharedPreferences sharedPrefs;

    @Inject
    public NotesSharedPrefs(Application application) {
        this.application = application;
        sharedPrefs = application.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
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
                application.getContentResolver().getPersistedUriPermissions();

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
        sharedPrefs
                .edit()
                .putInt(Constants.NIGHT_MODE, mode)
                .apply();
    }

    public int getNightMode() {
        return sharedPrefs.getInt(Constants.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setWorkingDir(Intent resultData) {
        // Get URI from result.
        Uri directoryUri = resultData.getData();
        // Persist the permission.
        final int flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        application.getContentResolver().takePersistableUriPermission(directoryUri, flags);
        // Save to SharedPrefs.
        String workingDir = directoryUri.toString();
        sharedPrefs
                .edit()
                .putString(Constants.WORKING_DIR_KEY, workingDir)
                .apply();
    }

    public String getWorkingDir() {
        return sharedPrefs.getString(Constants.WORKING_DIR_KEY, null);
    }
}
