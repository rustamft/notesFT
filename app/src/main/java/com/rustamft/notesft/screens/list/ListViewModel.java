package com.rustamft.notesft.screens.list;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rustamft.notesft.database.Repository;
import com.rustamft.notesft.database.SharedPrefs;
import com.rustamft.notesft.models.File;
import com.rustamft.notesft.models.NoteFile;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ListViewModel extends ViewModel {

    private final Application application;
    private final SharedPrefs prefs;
    private final Repository repository;
    private final MutableLiveData<List<String>> notesList = new MutableLiveData<>();
    private String appVersion = "Not available";

    @Inject
    ListViewModel(
            Application application,
            SharedPrefs prefs,
            Repository repository
    ) {
        this.application = application;
        this.prefs = prefs;
        this.repository = repository;
    }

    /**
     * Checks if the app has the files read/write permission.
     *
     * @return true if the permission is granted, false otherwise.
     */
    boolean hasPermission() {
        return prefs.hasPermission();
    }

    /**
     * Getter for LiveData of note files array.
     *
     * @return the MutableLiveData stored in the ViewModel.
     */
    MutableLiveData<List<String>> getNotesListLiveData() {
        return notesList;
    }

    /**
     * Reads the working directory contents and builds an updated note files list.
     */
    void updateNotesList() {
        repository.updateFilesList(prefs.getWorkingDir(), notesList);
    }

    void setNightMode(int mode) {
        prefs.setNightMode(mode);
    }

    int getNightMode() {
        return prefs.getNightMode();
    }

    /**
     * Getter for a note name at the given position.
     *
     * @param position a position in the notes list.
     * @return a String with the note name.
     */
    String getNoteNameAtPosition(int position) {
        return Objects.requireNonNull(notesList.getValue()).get(position);
    }

    /**
     * Deletes a note file with the given name.
     *
     * @param noteName a name of a note to be deleted.
     */
    void deleteNote(String noteName) {
        File file = new NoteFile(
                application.getApplicationContext(),
                prefs.getWorkingDir(),
                noteName
        );
        repository.deleteFile(file, notesList);
    }

    /**
     * Creates a note file with the given name.
     *
     * @param noteName a name of a note to be created.
     * @return true if the file has been created successfully, otherwise - false.
     */
    boolean createNote(String noteName) {
        File file = new NoteFile(
                application.getApplicationContext(),
                prefs.getWorkingDir(),
                noteName
        );
        return repository.createFile(file);
    }

    /**
     * Getter for the app version stored in the ViewModel, if there's none stored it builds one.
     *
     * @return a String with the app version or "Not available" if it couldn't build one.
     */
    String getAppVersion() {
        if (appVersion.equals("Not available")) {
            try {
                Context context = application.getApplicationContext();
                PackageInfo packageInfo =
                        context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                appVersion = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return appVersion;
    }
}