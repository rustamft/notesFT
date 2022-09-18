package com.rustamft.notesft.data.storage.shared;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.rustamft.notesft.data.model.AppPreferencesDataModel;
import com.rustamft.notesft.data.storage.AppPreferencesStorage;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class AppPreferencesSharedStorage implements AppPreferencesStorage {

    private final SharedPreferences mSharedPreferences;
    private static final String SHARED_PREFS_FILE_NAME = "com.rustamft.notesft.shared_preferences";
    private static final String KEY_NIGHT_MODE = "night_mode";
    public static final String KEY_WORKING_DIR = "working_dir";

    public AppPreferencesSharedStorage(Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, MODE_PRIVATE);
    }

    @Override
    public Single<Boolean> save(AppPreferencesDataModel appPreferences) {
        return Single.create(emitter -> {
                    mSharedPreferences
                            .edit()
                            .putInt(KEY_NIGHT_MODE, appPreferences.nightMode)
                            .putString(KEY_WORKING_DIR, appPreferences.workingDir)
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
                                mSharedPreferences.getInt(KEY_NIGHT_MODE, 0),
                                mSharedPreferences.getString(KEY_WORKING_DIR, null)
                        )
                );
            }
        });
    }
}
