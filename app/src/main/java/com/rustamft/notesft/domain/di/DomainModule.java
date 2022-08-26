package com.rustamft.notesft.domain.di;

import android.content.Context;

import com.rustamft.notesft.domain.util.PermissionChecker;
import com.rustamft.notesft.domain.util.ToastDisplay;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DomainModule {

    @Provides
    @Singleton
    PermissionChecker providePermissionChecker(@ApplicationContext Context context) {
        return new PermissionChecker(context);
    }

    @Provides
    @Singleton
    ToastDisplay provideToastDisplay(@ApplicationContext Context context) {
        return new ToastDisplay(context);
    }
}
