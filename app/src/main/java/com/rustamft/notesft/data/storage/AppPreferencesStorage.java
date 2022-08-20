package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.model.AppPreferencesData;

public interface AppPreferencesStorage {

    void save(AppPreferencesData appPreferences);
    AppPreferencesData get();
}
