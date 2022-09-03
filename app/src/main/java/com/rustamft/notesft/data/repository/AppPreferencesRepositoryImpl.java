package com.rustamft.notesft.data.repository;

import com.rustamft.notesft.data.model.AppPreferencesDataModel;
import com.rustamft.notesft.data.storage.AppPreferencesStorage;
import com.rustamft.notesft.domain.model.AppPreferences;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppPreferencesRepositoryImpl implements AppPreferencesRepository {

    private final AppPreferencesStorage mAppPreferencesStorage;

    public AppPreferencesRepositoryImpl(AppPreferencesStorage appPreferencesStorage) {
        mAppPreferencesStorage = appPreferencesStorage;
    }

    public Single<Boolean> saveAppPreferences(AppPreferences appPreferences) {
        return mAppPreferencesStorage.save(convertForData(appPreferences))
                .subscribeOn(Schedulers.io());
    }

    public Observable<AppPreferences> getAppPreferences() {
        return mAppPreferencesStorage.get()
                .map(this::convertForDomain)
                .subscribeOn(Schedulers.io());
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
