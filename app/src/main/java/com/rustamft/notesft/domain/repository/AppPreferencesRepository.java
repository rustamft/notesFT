package com.rustamft.notesft.domain.repository;

import com.rustamft.notesft.domain.model.AppPreferences;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public interface AppPreferencesRepository {

    Completable save(AppPreferences appPreferences);

    Observable<AppPreferences> observe();
}
