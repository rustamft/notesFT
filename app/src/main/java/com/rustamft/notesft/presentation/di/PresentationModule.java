package com.rustamft.notesft.presentation.di;

import com.rustamft.notesft.presentation.navigation.Navigator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class PresentationModule {

    @Provides
    @Singleton
    Navigator provideNavigator() {
        return new Navigator();
    }
}
