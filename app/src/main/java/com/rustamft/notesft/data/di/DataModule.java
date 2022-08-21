package com.rustamft.notesft.data.di;

import android.content.Context;

import com.rustamft.notesft.data.repository.AppPreferencesRepositoryImpl;
import com.rustamft.notesft.data.repository.NoteRepositoryImpl;
import com.rustamft.notesft.data.storage.AppPreferencesStorage;
import com.rustamft.notesft.data.storage.NoteStorage;
import com.rustamft.notesft.data.storage.disk.DiskNoteStorage;
import com.rustamft.notesft.data.storage.sharedpreferences.SharedPreferencesStorage;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.repository.NoteRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
class DataModule {

    @Provides
    @Singleton
    AppPreferencesStorage provideAppPreferencesStorage(@ApplicationContext Context context) {
        return new SharedPreferencesStorage(context);
    }

    @Provides
    @Singleton
    AppPreferencesRepository provideAppPreferencesRepository(
            @ApplicationContext Context context,
            AppPreferencesStorage appPreferencesStorage
    ) {
        return new AppPreferencesRepositoryImpl(context, appPreferencesStorage);
    }

    @Provides
    @Singleton
    NoteStorage provideNoteStorage(@ApplicationContext Context context) {
        return new DiskNoteStorage(context);
    }

    @Provides
    @Singleton
    NoteRepository provideNotesRepository(
            @ApplicationContext Context context,
            NoteStorage noteStorage
    ) {
        return new NoteRepositoryImpl(context, noteStorage);
    }
}
