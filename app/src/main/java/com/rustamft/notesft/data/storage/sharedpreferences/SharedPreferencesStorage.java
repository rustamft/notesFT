package com.rustamft.notesft.data.storage.sharedpreferences;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.rustamft.notesft.data.model.AppPreferencesData;
import com.rustamft.notesft.data.storage.AppPreferencesStorage;
import com.rustamft.notesft.util.Constants;

public class SharedPreferencesStorage implements AppPreferencesStorage {

    private final SharedPreferences sharedPrefs;

    public SharedPreferencesStorage(Context context) {
        sharedPrefs = context.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
    }

    @Override
    public void save(AppPreferencesData appPreferences) {
        sharedPrefs
                .edit()
                .putInt(Constants.NIGHT_MODE, appPreferences.nightMode)
                .putString(Constants.WORKING_DIR_KEY, appPreferences.workingDir)
                .apply();
    }

    @Override
    public AppPreferencesData get() {
        int nightMode = sharedPrefs.getInt(Constants.NIGHT_MODE, 0);
        String workingDir = sharedPrefs.getString(Constants.WORKING_DIR_KEY, null);
        return new AppPreferencesData(nightMode, workingDir);
    }
}
