package com.rustamft.notesft.presentation.screen.permission;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rustamft.notesft.R;
import com.rustamft.notesft.domain.model.AppPreferences;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.util.ToastDisplay;
import com.rustamft.notesft.presentation.navigation.Navigator;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class PermissionViewModel extends ViewModel {

    private final AppPreferencesRepository mAppPreferencesRepository;
    private final ToastDisplay mToastDisplay;
    private final Navigator mNavigator;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final MutableLiveData<AppPreferences> mAppPreferences = new MutableLiveData<>();

    @Inject
    PermissionViewModel(
            AppPreferencesRepository appPreferencesRepository,
            ToastDisplay toastDisplay,
            Navigator navigator
    ) {
        mAppPreferencesRepository = appPreferencesRepository;
        mToastDisplay = toastDisplay;
        mNavigator = navigator;
        mDisposables.add(
                mAppPreferencesRepository.getAppPreferences()
                        .subscribe(mAppPreferences::postValue)
        );
    }

    @Override
    protected void onCleared() {
        mDisposables.clear();
        super.onCleared();
    }

    protected void saveWorkingDirPreference(Intent resultData, View view) {
        AppPreferences appPreferences = mAppPreferences.getValue();
        if (appPreferences == null) return;
        Uri workingDirUri = resultData.getData();
        persistWorkingDirPermission(workingDirUri, view.getContext().getApplicationContext());
        AppPreferences.CopyBuilder appPreferencesCopyBuilder =
                mAppPreferences.getValue().copyBuilder();
        appPreferencesCopyBuilder.setWorkingDir(workingDirUri.toString());
        mDisposables.add(
                mAppPreferencesRepository.saveAppPreferences(
                                appPreferencesCopyBuilder.build()
                        )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                success -> {
                                    if (success) navigateNext();
                                },
                                error -> mToastDisplay.showLong(error.getMessage())
                        )
        );
    }

    private void persistWorkingDirPermission(Uri workingDirUri, Context context) {
        final int flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        context.getContentResolver().takePersistableUriPermission(workingDirUri, flags);
    }

    private void navigateNext() {
        mNavigator.navigate(R.id.action_permissionFragment_to_listFragment);
    }
}
