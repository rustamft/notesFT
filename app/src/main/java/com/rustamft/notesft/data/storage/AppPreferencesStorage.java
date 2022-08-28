package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.model.AppPreferencesDataModel;

public interface AppPreferencesStorage {

    boolean save(AppPreferencesDataModel appPreferences);
    AppPreferencesDataModel get();
}
