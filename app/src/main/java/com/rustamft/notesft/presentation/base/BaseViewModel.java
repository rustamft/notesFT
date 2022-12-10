package com.rustamft.notesft.presentation.base;

import androidx.lifecycle.ViewModel;

import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.repository.NoteRepository;
import com.rustamft.notesft.presentation.navigation.Navigator;
import com.rustamft.notesft.presentation.toast.ToastDisplay;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class BaseViewModel extends ViewModel {

    protected AppPreferencesRepository mAppPreferencesRepository;
    protected NoteRepository mNoteRepository;
    protected ToastDisplay mToastDisplay;
    protected Navigator mNavigator;
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    protected void onCleared() {
        super.onCleared();
        mDisposables.clear();
    }

    protected void disposeLater(@NonNull Disposable disposable) {
        mDisposables.add(disposable);
    }
}
