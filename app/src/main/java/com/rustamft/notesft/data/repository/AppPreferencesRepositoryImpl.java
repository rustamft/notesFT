package com.rustamft.notesft.data.repository;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.rustamft.notesft.data.storage.AppPreferencesStorage;
import com.rustamft.notesft.data.model.AppPreferencesDataModel;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;

public class AppPreferencesRepositoryImpl implements AppPreferencesRepository {

    private final Context mContext;
    private final AppPreferencesStorage mAppPreferencesStorage;
    private AppPreferencesDataModel mAppPreferences;

    public AppPreferencesRepositoryImpl(
            Context context,
            AppPreferencesStorage appPreferencesStorage
    ) {
        mContext = context;
        mAppPreferencesStorage = appPreferencesStorage;
        mAppPreferences = appPreferencesStorage.get();
    }

    public void setNightMode(int nightMode) {
        mAppPreferencesStorage.save(
                new AppPreferencesDataModel(
                        nightMode,
                        mAppPreferences.workingDir
                )
        );
    }

    public void setWorkingDir(Intent resultData) {
        Uri directoryUri = resultData.getData(); // Get URI from result
        final int flags = // Persist the permission
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        mContext.getContentResolver().takePersistableUriPermission(directoryUri, flags);
        String workingDir = directoryUri.toString();
        mAppPreferencesStorage.save(
                new AppPreferencesDataModel(
                        mAppPreferences.nightMode,
                        workingDir
                )
        );
    }

    public AppPreferencesDataModel getAppPreferences() {
        mAppPreferences = mAppPreferencesStorage.get();
        return mAppPreferences;
    }
}
