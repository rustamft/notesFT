package com.rustamft.notesft.data.repository;

import com.rustamft.notesft.data.model.AppPreferencesDataModel;
import com.rustamft.notesft.data.storage.AppPreferencesStorage;
import com.rustamft.notesft.domain.model.AppPreferences;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppPreferencesRepositoryImpl implements AppPreferencesRepository {

    private final AppPreferencesStorage mAppPreferencesStorage;

    public AppPreferencesRepositoryImpl(AppPreferencesStorage appPreferencesStorage) {
        mAppPreferencesStorage = appPreferencesStorage;
    }

    public Single<Boolean> saveAppPreferences(AppPreferences appPreferences) {
        return Single.fromCallable(
                        () -> mAppPreferencesStorage.save(
                                convertForData(appPreferences)
                        )
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<AppPreferences> getAppPreferences() {
        return Single.fromCallable(
                        () -> convertForDomain(
                                mAppPreferencesStorage.get()
                        )
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private AppPreferencesDataModel convertForData(AppPreferences appPreferences) {
        return new AppPreferencesDataModel(
                appPreferences.nightMode,
                appPreferences.workingDir
        );
    }

    private AppPreferences convertForDomain(AppPreferencesDataModel appPreferences) {
        return new AppPreferences(
                appPreferences.nightMode,
                appPreferences.workingDir
        );
    }
}
