package com.rustamft.notesft.domain.model;

public abstract class AppPreferences {

    public final int nightMode;
    public final String workingDir;

    public AppPreferences(
            int nightMode,
            String workingDir
    ) {
        this.nightMode = nightMode;
        this.workingDir = workingDir;
    }
}
