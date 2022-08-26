package com.rustamft.notesft.presentation.screen.permission;

import android.content.Intent;

import androidx.lifecycle.ViewModel;

import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.util.PermissionChecker;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PermissionViewModel extends ViewModel {

    private final AppPreferencesRepository mAppPreferencesRepository;
    private final PermissionChecker mPermissionChecker;

    @Inject
    PermissionViewModel(
            AppPreferencesRepository appPreferencesRepository,
            PermissionChecker permissionChecker
    ) {
        mAppPreferencesRepository = appPreferencesRepository;
        mPermissionChecker = permissionChecker;
    }

    boolean hasWorkingDirPermission() {
        return mPermissionChecker.hasWorkingDirPermission(
                mAppPreferencesRepository.getAppPreferences().workingDir
        );
    }

    void setWorkingDir(Intent resultData) {
        mAppPreferencesRepository.setWorkingDir(resultData);
    }
}
