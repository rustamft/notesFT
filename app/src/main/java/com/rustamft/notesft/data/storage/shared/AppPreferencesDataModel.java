package com.rustamft.notesft.data.storage.shared;

import com.rustamft.notesft.domain.model.AppPreferences;

public class AppPreferencesDataModel extends AppPreferences {

    public AppPreferencesDataModel(
            int nightMode,
            String workingDir
    ) {
        super(nightMode, workingDir);
    }
}
