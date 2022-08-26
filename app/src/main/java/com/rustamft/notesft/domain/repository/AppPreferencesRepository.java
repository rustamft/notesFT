package com.rustamft.notesft.domain.repository;

import android.content.Intent;

import com.rustamft.notesft.domain.model.AppPreferences;

public interface AppPreferencesRepository {

    void setNightMode(int mode);

    void setWorkingDir(Intent resultData);

    AppPreferences getAppPreferences();
}
