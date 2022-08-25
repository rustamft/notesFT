package com.rustamft.notesft.data.storage.sharedpreferences;

import com.rustamft.notesft.domain.model.AppPreferences;

public class AppPreferencesData extends AppPreferences { // TODO: use this

    public final int nightMode;
    public final String workingDir;

    public AppPreferencesData(int nightMode, String workingDir) {
        this.nightMode = nightMode;
        this.workingDir = workingDir;
    }
}
