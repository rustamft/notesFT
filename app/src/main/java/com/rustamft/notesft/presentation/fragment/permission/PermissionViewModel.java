package com.rustamft.notesft.presentation.fragment.permission;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.rustamft.notesft.domain.model.AppPreferences;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.presentation.base.BaseViewModel;
import com.rustamft.notesft.presentation.navigation.Navigator;
import com.rustamft.notesft.presentation.navigation.Route;
import com.rustamft.notesft.presentation.toast.ToastDisplay;

import javax.inject.Inject;

import androidx.lifecycle.MutableLiveData;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

@HiltViewModel
public class PermissionViewModel extends BaseViewModel {

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
        disposeLater(
                mAppPreferencesRepository.observe().subscribe(mAppPreferences::postValue)
        );
    }

    protected void saveWorkingDirPreference(Intent resultData, View view) {
        AppPreferences appPreferences = mAppPreferences.getValue();
        if (appPreferences == null) return;
        Uri workingDirUri = resultData.getData();
        persistWorkingDirPermission(workingDirUri, view.getContext().getApplicationContext());
        AppPreferences.CopyBuilder appPreferencesCopyBuilder =
                mAppPreferences.getValue().copyBuilder();
        appPreferencesCopyBuilder.setWorkingDir(workingDirUri.toString());
        Disposable saveAppPreferencesDisposable = mAppPreferencesRepository.save(appPreferencesCopyBuilder.build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> mNavigator.navigate(Route.PERMISSION_TO_LIST),
                        error -> mToastDisplay.showLong(error.getMessage())
                );
        disposeLater(saveAppPreferencesDisposable);
    }

    private void persistWorkingDirPermission(Uri workingDirUri, Context context) {
        final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        context.getContentResolver().takePersistableUriPermission(workingDirUri, flags);
    }
}
