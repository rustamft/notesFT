package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.storage.shared.AppPreferencesDataModel;

public interface AppPreferencesStorage {

    void save(AppPreferencesDataModel appPreferences);
    AppPreferencesDataModel get();
}
