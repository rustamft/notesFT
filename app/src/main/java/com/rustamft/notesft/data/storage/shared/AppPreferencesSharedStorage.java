package com.rustamft.notesft.data.storage.shared;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.rustamft.notesft.data.model.AppPreferencesDataModel;
import com.rustamft.notesft.data.storage.AppPreferencesStorage;
import com.rustamft.notesft.domain.util.Constants;

public class AppPreferencesSharedStorage implements AppPreferencesStorage {

    private final SharedPreferences sharedPrefs;

    public AppPreferencesSharedStorage(Context context) {
        sharedPrefs = context.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
    }

    @Override
    public void save(AppPreferencesDataModel appPreferences) {
        sharedPrefs
                .edit()
                .putInt(Constants.NIGHT_MODE, appPreferences.nightMode)
                .putString(Constants.WORKING_DIR_KEY, appPreferences.workingDir)
                .apply();
    }

    @Override
    public AppPreferencesDataModel get() {
        int nightMode = sharedPrefs.getInt(Constants.NIGHT_MODE, 0);
        String workingDir = sharedPrefs.getString(Constants.WORKING_DIR_KEY, null);
        return new AppPreferencesDataModel(nightMode, workingDir);
    }
}
