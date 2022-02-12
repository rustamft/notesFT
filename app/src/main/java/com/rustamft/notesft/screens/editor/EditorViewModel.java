package com.rustamft.notesft.screens.editor;

import android.app.Application;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.rustamft.notesft.R;
import com.rustamft.notesft.activities.MainActivity;
import com.rustamft.notesft.database.Repository;
import com.rustamft.notesft.database.SharedPrefs;
import com.rustamft.notesft.models.File;
import com.rustamft.notesft.models.NoteFile;
import com.rustamft.notesft.utils.Constants;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditorViewModel extends ViewModel implements LifecycleObserver {

    private final Application application;
    private final Repository repository;
    private final MutableLiveData<String> actionBarTitle = new MutableLiveData<>();
    private androidx.lifecycle.Observer<String> actionBarTitleObserver;
    private File note;
    public String noteText;

    @Inject
    public EditorViewModel(
            Application application,
            SavedStateHandle state,
            SharedPrefs prefs,
            Repository repository
    ) {
        this.application = application;
        this.repository = repository;
        String name = state.get(Constants.NOTE_NAME);
        actionBarTitle.setValue(name);
        if (isNotNullNorBlank(name)) {
            this.note = new NoteFile(
                    application.getApplicationContext(),
                    prefs.getWorkingDir(),
                    name
            );
            this.noteText = note.buildStringFromContent();
        }
    }

    boolean isNotNullNorBlank(String string) {
        if (string == null) {
            return false;
        }
        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes the ActionBar title LiveData to be observed.
     *
     * @param mainActivity the app's MainActivity.
     */
    void registerActionBarTitleObserver(MainActivity mainActivity) {
        ActionBar actionBar = mainActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBarTitleObserver = s -> actionBar.setTitle(actionBarTitle.getValue());
            actionBarTitle.observe(mainActivity, actionBarTitleObserver);
        }
    }


    /**
     * Sets the app name to the ActionBar title.
    */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void resetActionBarTitle() {
        actionBarTitle.setValue(application.getString(R.string.app_name));
        actionBarTitle.removeObserver(actionBarTitleObserver);
    }

    /**
     * Saves the given text to the current note file.
     *
     * @param text a text to save to the current note.
     */
    void saveNoteText(String text) {
        repository.saveFile(note, text);
    }

    /**
     * Renames the current note file.
     *
     * @param newName a new nate for the note.
     */
    void renameNote(String newName) {
        repository.renameFile(note, newName, actionBarTitle);
    }

    /**
     * The current note file full path getter.
     *
     * @return a String with a full path.
     */
    String getNotePath() {
        return note.path();
    }

    /**
     * The current note file size getter.
     *
     * @return a long with the file length.
     */
    long getNoteSize() {
        return repository.getFileLength(note);
    }

    /**
     * The current note file last modified made getter.
     *
     * @return a String with the file last modified formatted date.
     */
    String getNoteLastModified() {
        return repository.lastModified(note);
    }

    /**
     * The current note file name getter.
     *
     * @return a String with the file name.
     */
    String getNoteName() {
        return repository.getFileName(note);
    }

    private void displayShortToast(String message) {
        Toast.makeText(application, message, Toast.LENGTH_SHORT).show();
    }

    private void displayLongToast(String message) {
        Toast.makeText(application, message, Toast.LENGTH_LONG).show();
    }
}
