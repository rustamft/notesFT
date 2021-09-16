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

    boolean hasPermission() {
        return mNotesRepository.hasPermission();
    }

    void setWorkingDir(Intent resultData) {
        mNotesRepository.setWorkingDir(resultData);
    }
}