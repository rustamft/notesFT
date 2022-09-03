package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.model.AppPreferencesDataModel;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface AppPreferencesStorage {

    Single<Boolean> save(AppPreferencesDataModel appPreferences);

    Observable<AppPreferencesDataModel> get();
}
