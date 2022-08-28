package com.rustamft.notesft.presentation.screen.permission;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.rustamft.notesft.R;
import com.rustamft.notesft.domain.model.AppPreferences;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.util.PermissionChecker;
import com.rustamft.notesft.domain.util.ToastDisplay;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class PermissionViewModel extends ViewModel {

    private final AppPreferencesRepository mAppPreferencesRepository;
    private final PermissionChecker mPermissionChecker;
    private final ToastDisplay mToastDisplay;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final MutableLiveData<AppPreferences> mAppPreferences = new MutableLiveData<>();

    @Inject
    PermissionViewModel(
            AppPreferencesRepository appPreferencesRepository,
            PermissionChecker permissionChecker,
            ToastDisplay toastDisplay
    ) {
        mAppPreferencesRepository = appPreferencesRepository;
        mPermissionChecker = permissionChecker;
        mToastDisplay = toastDisplay;
        mDisposables.add(
                mAppPreferencesRepository.getAppPreferences().subscribe(mAppPreferences::postValue)
        );
    }

    @Override
    protected void onCleared() {
        mDisposables.clear();
        super.onCleared();
    }

    void navigateNext(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_permissionFragment_to_listFragment);
    }

    protected void checkDirPermission(View view) {
        mDisposables.add(
                mAppPreferencesRepository.getAppPreferences().subscribe(appPreferences -> {
                    if (mPermissionChecker.hasWorkingDirPermission(appPreferences.workingDir)) {
                        navigateNext(view);
                    }
                })
        );
    }

    protected void saveWorkingDirPreference(Intent resultData, View view) {
        AppPreferences appPreferences = mAppPreferences.getValue();
        if (appPreferences == null) return;
        String workingDir = extractWorkingDirFromResult(resultData, view.getContext());
        AppPreferences.CopyBuilder appPreferencesCopyBuilder =
                mAppPreferences.getValue().copyBuilder();
        appPreferencesCopyBuilder.setWorkingDir(workingDir);
        mDisposables.add(
                mAppPreferencesRepository.saveAppPreferences(
                        appPreferencesCopyBuilder.build()
                ).subscribe(
                        success -> {
                            if (success) navigateNext(view);
                        },
                        error -> mToastDisplay.showLong(error.getMessage())
                )
        );
    }

    private String extractWorkingDirFromResult(Intent resultData, Context context) {
        Uri directoryUri = resultData.getData(); // Get URI from result
        final int flags = // Persist the permission
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        context.getContentResolver().takePersistableUriPermission(directoryUri, flags);
        return directoryUri.toString();
    }
}
