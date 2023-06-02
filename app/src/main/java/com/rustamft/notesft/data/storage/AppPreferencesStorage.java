package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.model.AppPreferencesDataModel;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public interface AppPreferencesStorage {

    Completable save(AppPreferencesDataModel appPreferences);

    Observable<AppPreferencesDataModel> observe();
}
