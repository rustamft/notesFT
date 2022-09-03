package com.rustamft.notesft.data.storage.shared;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.rustamft.notesft.data.model.AppPreferencesDataModel;
import com.rustamft.notesft.data.storage.AppPreferencesStorage;
import com.rustamft.notesft.domain.util.Constants;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class AppPreferencesSharedStorage implements AppPreferencesStorage {

    private final SharedPreferences mSharedPreferences;

    public AppPreferencesSharedStorage(Context context) {
        mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);
    }

    @Override
    public Single<Boolean> save(AppPreferencesDataModel appPreferences) {
        return Single.create(emitter -> {
                    mSharedPreferences
                            .edit()
                            .putInt(Constants.NIGHT_MODE, appPreferences.nightMode)
                            .putString(Constants.WORKING_DIR, appPreferences.workingDir)
                            .apply();
                    if (!emitter.isDisposed()) {
                        emitter.onSuccess(true);
                    }
                }
        );
    }

    @Override
    public Observable<AppPreferencesDataModel> get() {
        return Observable.create(emitter -> {
            if (!emitter.isDisposed()) {
                emitter.onNext(
                        new AppPreferencesDataModel(
                                mSharedPreferences.getInt(Constants.NIGHT_MODE, 0),
                                mSharedPreferences.getString(Constants.WORKING_DIR, null)
                        )
                );
            }
            mSharedPreferences.registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> {
                if (!emitter.isDisposed()) {
                    emitter.onNext(
                            new AppPreferencesDataModel(
                                    sharedPreferences.getInt(Constants.NIGHT_MODE, 0),
                                    sharedPreferences.getString(Constants.WORKING_DIR, null)
                            )
                    );
                }
            });
        });
    }
}
