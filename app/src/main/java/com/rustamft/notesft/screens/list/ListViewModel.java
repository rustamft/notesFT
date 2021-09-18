package com.rustamft.notesft.screens.list;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.rustamft.notesft.database.NotesRepository;
import com.rustamft.notesft.models.Note;

public class ListViewModel extends AndroidViewModel {
    private final NotesRepository mNotesRepository;
    private final MutableLiveData<String[]> mNotesListLiveData = new MutableLiveData<>();
    private String mAppVersion = "Not available";
    private final Application mApplication;

    public ListViewModel(@NonNull Application application) {
        super(application);

        mNotesRepository = NotesRepository.getInstance(application);
        mApplication = application;
    }

    /**
     * Checks if the app has the files read/write permission.
     * @return true if the permission is granted, otherwise - false.
     */
    boolean hasPermission() {
        return mNotesRepository.hasPermission();
    }

    /**
     * Getter for LiveData of note files array.
     * @return the MutableLiveData stored in the ViewModel.
     */
    MutableLiveData<String[]> getNotesListLiveData() {
        return mNotesListLiveData;
    }

    /**
     * Reads the working directory contents and builds an updated note files list.
     */
    void updateNotesList() {
        mNotesRepository.updateFilesList(mNotesListLiveData);
    }

    /**
     * Getter for a note name at the given position.
     * @param position a position in the notes list.
     * @return a String with the note name.
     */
    String getNoteNameAtPosition(int position) {
        String[] notesList = mNotesListLiveData.getValue();
        if (notesList != null) {
            return notesList[position];
        } else return null;
    }

    /**
     * Deletes a note file with the given name.
     * @param noteName a name of a note to be deleted.
     */
    void deleteNote(String noteName) {
        Note note = mNotesRepository.getFileInstance(noteName);
        mNotesRepository.deleteFile(note, mNotesListLiveData);
    }

    /**
     * Creates a note file with the given name.
     * @param noteName a name of a note to be created.
     * @return true if the file has been created successfully, otherwise - false.
     */
    boolean createNote(String noteName) {
        Note note = mNotesRepository.getFileInstance(noteName);
        return mNotesRepository.createNewFile(note);
    }

    /**
     * Getter for the app version stored in the ViewModel, if there's none stored it builds one.
     * @return a String with the app version or "Not available" if it couldn't build one.
     */
    String getAppVersion() {
        if (mAppVersion.equals("Not available")) {
            try {
                Context context = mApplication.getApplicationContext();
                PackageInfo packageInfo = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0);
                mAppVersion = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mAppVersion;
    }
}