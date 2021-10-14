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
import com.rustamft.notesft.database.Repository;
import com.rustamft.notesft.models.File;
import com.rustamft.notesft.utils.DIC;

public class EditorViewModel extends AndroidViewModel implements LifecycleObserver {
    private final Repository mNotesRepository;
    private final MutableLiveData<String> mLiveDataToolbarTitle = new MutableLiveData<>();
    private File mCurrentNote;
    private Observer<String> mObserverToolbarTitle;

    public EditorViewModel(@NonNull Application application) {
        super(application);

        DIC dic = new DIC(application);
        mNotesRepository = dic.getRepository();
    }

    /**
     * Makes the ActionBar title LiveData to be observed.
     * @param mainActivity the app's MainActivity.
     */
    void registerActionBarTitleObserver(MainActivity mainActivity) {
        ActionBar actionBar = mainActivity.getSupportActionBar();
        if (actionBar != null) {
            mObserverToolbarTitle = s -> actionBar.setTitle(mLiveDataToolbarTitle.getValue());
            mLiveDataToolbarTitle.observe(mainActivity, mObserverToolbarTitle);
        }
    }

    /**
     * Sets the app name to the ActionBar title.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void resetActionBarTitle() {
        mLiveDataToolbarTitle.setValue(getApplication().getString(R.string.app_name));
        mLiveDataToolbarTitle.removeObserver(mObserverToolbarTitle);
    }

    /**
     * Sets an instance of a note for ViewModel to work with.
     * @param name the name of a note.
     */
    void setCurrentNote(String name) {
        DIC dic = new DIC(getApplication());
        mCurrentNote = dic.getFileInstance(name);
        mLiveDataToolbarTitle.setValue(name); // Set toolbar title
    }

    /**
     * Reads a text the current note file contains.
     * @return a String with the current note text.
     */
    String getNoteText() {
        return mNotesRepository.getFileText(mCurrentNote);
    }

    /**
     * Saves the given text to the current note file.
     * @param text a text to save to the current note.
     */
    void saveTextToNote(String text) {
        mNotesRepository.saveFile(mCurrentNote, text);
    }

    /**
     * Renames the current note file.
     * @param newName a new nate for the note.
     */
    void renameNote(String newName) {
        mNotesRepository.renameFile(mCurrentNote, newName, mLiveDataToolbarTitle);
    }

    /**
     * The current note file full path getter.
     * @return a String with a full path.
     */
    String getNotePath() {
        return mCurrentNote.path();
    }

    /**
     * The current note file size getter.
     * @return a long with the file length.
     */
    long getNoteSize() {
        return mNotesRepository.getFileLength(mCurrentNote);
    }

    /**
     * The current note file last modified made getter.
     * @return a String with the file last modified formatted date.
     */
    String getNoteLastModified() {
        return mNotesRepository.lastModified(mCurrentNote);
    }

    /**
     * The current note file name getter.
     * @return a String with the file name.
     */
    String getNoteName() {
        return mNotesRepository.getFileName(mCurrentNote);
    }
}
