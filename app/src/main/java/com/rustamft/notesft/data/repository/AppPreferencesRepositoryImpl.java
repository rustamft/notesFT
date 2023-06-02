package com.rustamft.notesft.data.repository;

import com.rustamft.notesft.data.model.AppPreferencesDataModel;
import com.rustamft.notesft.data.storage.AppPreferencesStorage;
import com.rustamft.notesft.domain.model.AppPreferences;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppPreferencesRepositoryImpl implements AppPreferencesRepository {

    private final AppPreferencesStorage mAppPreferencesStorage;

    public AppPreferencesRepositoryImpl(AppPreferencesStorage appPreferencesStorage) {
        mAppPreferencesStorage = appPreferencesStorage;
    }

    public Completable save(AppPreferences appPreferences) {
        return mAppPreferencesStorage.save(map(appPreferences)).subscribeOn(Schedulers.io());
    }

    public Observable<AppPreferences> observe() {
        return mAppPreferencesStorage.observe()
                .map(this::map)
                .subscribeOn(Schedulers.io());
    }

    private AppPreferencesDataModel map(AppPreferences appPreferences) {
        return new AppPreferencesDataModel(
                appPreferences.nightMode,
                appPreferences.workingDir
        );
    }

    private AppPreferences map(AppPreferencesDataModel appPreferences) {
        return new AppPreferences(
                appPreferences.nightMode,
                appPreferences.workingDir
        );
    }
}
