package com.rustamft.notesft.screens.permission;

import android.content.Intent;

import androidx.lifecycle.ViewModel;

import com.rustamft.notesft.database.SharedPrefs;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PermissionViewModel extends ViewModel {
    private final SharedPrefs prefs;

    @Inject
    PermissionViewModel(SharedPrefs prefs) {
        this.prefs = prefs;
    }

    /**
     * Checks if the app has the files read/write permission.
     *
     * @return true if the permission is granted, otherwise - false.
     */
    boolean hasPermission() {
        return prefs.hasPermission();
    }

    /**
     * Sets the working directory for the app.
     *
     * @param resultData a data result from a folder chooser intent.
     */
    void setWorkingDir(Intent resultData) {
        prefs.setWorkingDir(resultData);
    }
}
