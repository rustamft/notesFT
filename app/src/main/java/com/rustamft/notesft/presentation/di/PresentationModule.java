package com.rustamft.notesft.presentation.di;

import android.content.Context;

import com.rustamft.notesft.presentation.navigation.Navigator;
import com.rustamft.notesft.presentation.permission.PermissionChecker;
import com.rustamft.notesft.presentation.toast.ToastDisplay;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class PresentationModule {

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

    @Provides
    @Singleton
    Navigator provideNavigator() {
        return new Navigator();
    }
}
