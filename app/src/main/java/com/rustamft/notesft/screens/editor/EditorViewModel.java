package com.rustamft.notesft.screens.editor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

import com.rustamft.notesft.R;
import com.rustamft.notesft.activities.MainActivity;
import com.rustamft.notesft.database.NotesRepository;
import com.rustamft.notesft.models.Note;

public class EditorViewModel extends AndroidViewModel implements LifecycleObserver {
    private final NotesRepository mNotesRepository;
    private final MutableLiveData<String> mLiveDataToolbarTitle = new MutableLiveData<>();
    private final Application mApplication;
    private Note mCurrentNote;
    private Observer<String> mObserverToolbarTitle;

    public EditorViewModel(@NonNull Application application) {
        super(application);

        mNotesRepository = NotesRepository.getInstance(application);
        mApplication = application;
    }

    void registerToolbarTitleObserver(MainActivity mainActivity) {
        ActionBar actionBar = mainActivity.getSupportActionBar();
        if (actionBar != null) {
            mObserverToolbarTitle = s -> actionBar.setTitle(mLiveDataToolbarTitle.getValue());
            mLiveDataToolbarTitle.observe(mainActivity, mObserverToolbarTitle);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void resetToolbarTitle() {
        mLiveDataToolbarTitle.setValue(mApplication.getString(R.string.app_name));
        mLiveDataToolbarTitle.removeObserver(mObserverToolbarTitle);
    }

    void setCurrentNote(String name) {
        mCurrentNote = mNotesRepository.getFileInstance(name);
        mLiveDataToolbarTitle.setValue(name); // Set toolbar title
    }

    String getNoteText() {
        return mNotesRepository.getFileText(mCurrentNote);
    }

    void saveTextToNote(String text) {
        mNotesRepository.saveTextToFile(mCurrentNote, text);
    }

    void renameNote(String newName) {
        mNotesRepository.renameFile(mCurrentNote, newName, mLiveDataToolbarTitle);
    }

    String getNotePath() {
        return mCurrentNote.path();
    }

    long getNoteSize() {
        return mNotesRepository.getFileLength(mCurrentNote);
    }

    String getNoteLastModified() {
        return mNotesRepository.lastModified(mCurrentNote);
    }

    String getNoteName() {
        return mNotesRepository.getFileName(mCurrentNote);
    }
}
