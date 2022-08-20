package com.rustamft.notesft.data.repository;

import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;

import com.rustamft.notesft.data.model.AppPreferencesData;
import com.rustamft.notesft.data.storage.AppPreferencesStorage;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;

import java.util.List;

public class AppPreferencesRepositoryImpl implements AppPreferencesRepository {

    private final Context context;
    private final AppPreferencesStorage mAppPreferencesStorage;
    private AppPreferencesData mAppPreferences;

    public AppPreferencesRepositoryImpl(
            Context context,
            AppPreferencesStorage appPreferencesStorage
    ) {
        this.context = context;
        this.mAppPreferencesStorage = appPreferencesStorage;
        this.mAppPreferences = appPreferencesStorage.get();
    }

    public boolean hasPermission() { // TODO: move to App class probably
        // Get working dir
        String workingDir = getWorkingDir();
        // Check if there is saved workingDir in shared preferences
        if (workingDir == null) {
            return false;
        }
        // Check permission
        List<UriPermission> permissionsList =
                context.getContentResolver().getPersistedUriPermissions();

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

    public void setNightMode(int nightMode) {
        mAppPreferencesStorage.save(
                new AppPreferencesData(
                        nightMode,
                        mAppPreferences.workingDir
                )
        );
    }

    public int getNightMode() {
        mAppPreferences = mAppPreferencesStorage.get();
        return mAppPreferences.nightMode;
    }

    public void setWorkingDir(Intent resultData) {
        // Get URI from result.
        Uri directoryUri = resultData.getData();
        // Persist the permission.
        final int flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        context.getContentResolver().takePersistableUriPermission(directoryUri, flags);
        // Save to SharedPrefs.
        String workingDir = directoryUri.toString();
        mAppPreferencesStorage.save(
                new AppPreferencesData(
                        mAppPreferences.nightMode,
                        workingDir
                )
        );
    }

    public String getWorkingDir() {
        mAppPreferences = mAppPreferencesStorage.get();
        return mAppPreferences.workingDir;
    }
}
