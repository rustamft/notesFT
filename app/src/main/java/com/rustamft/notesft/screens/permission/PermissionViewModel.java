package com.rustamft.notesft.screens.permission;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.rustamft.notesft.database.NotesRepository;

public class PermissionViewModel extends AndroidViewModel {
    private final NotesRepository mNotesRepository;

    public PermissionViewModel(@NonNull Application application) {
        super(application);

        mNotesRepository = NotesRepository.getInstance(application);
    }

    /**
     * Checks if the app has the files read/write permission.
     * @return true if the permission is granted, otherwise - false.
     */
    boolean hasPermission() {
        return mNotesRepository.hasPermission();
    }

    /**
     * Sets the working directory for the app.
     * @param resultData a data result from a folder chooser intent.
     */
    void setWorkingDir(Intent resultData) {
        mNotesRepository.setWorkingDir(resultData);
    }
}