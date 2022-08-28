package com.rustamft.notesft.data.storage.shared;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.rustamft.notesft.data.model.AppPreferencesDataModel;
import com.rustamft.notesft.data.storage.AppPreferencesStorage;
import com.rustamft.notesft.domain.util.Constants;

public class AppPreferencesSharedStorage implements AppPreferencesStorage {

    private final SharedPreferences mSharedPreferences;

    public AppPreferencesSharedStorage(Context context) {
        mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
    }

    @Override
    public boolean save(AppPreferencesDataModel appPreferences) {
        mSharedPreferences
                .edit()
                .putInt(Constants.NIGHT_MODE, appPreferences.nightMode)
                .putString(Constants.WORKING_DIR, appPreferences.workingDir)
                .apply();
        return appPreferences == get();
    }

    @Override
    public AppPreferencesDataModel get() {
        int nightMode = mSharedPreferences.getInt(Constants.NIGHT_MODE, 0);
        String workingDir = mSharedPreferences.getString(Constants.WORKING_DIR, null);
        return new AppPreferencesDataModel(nightMode, workingDir);
    }
}
