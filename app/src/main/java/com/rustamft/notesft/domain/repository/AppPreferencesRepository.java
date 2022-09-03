package com.rustamft.notesft.domain.repository;

import com.rustamft.notesft.domain.model.AppPreferences;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface AppPreferencesRepository {

    Single<Boolean> saveAppPreferences(AppPreferences appPreferences);

    Observable<AppPreferences> getAppPreferences();
}
