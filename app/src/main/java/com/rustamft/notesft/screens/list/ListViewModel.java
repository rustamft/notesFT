package com.rustamft.notesft.screens.list;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.rustamft.notesft.database.Repository;
import com.rustamft.notesft.database.SharedPrefs;
import com.rustamft.notesft.models.File;
import com.rustamft.notesft.utils.Dic;

import java.util.List;
import java.util.Objects;

public class ListViewModel extends AndroidViewModel {
    private final Repository mNotesRepository;
    private final SharedPrefs mSharedPrefs;
    private final Dic mDic;
    private final MutableLiveData<List<String>> mNotesListLiveData = new MutableLiveData<>();
    private String mAppVersion = "Not available";

    public ListViewModel(@NonNull Application application) {
        super(application);

        mDic = new Dic(application);
        mNotesRepository = mDic.getRepository();
        mSharedPrefs = mDic.getSharedPrefs();
    }

    /**
     * Checks if the app has the files read/write permission.
     * @return true if the permission is granted, false otherwise.
     */
    boolean hasPermission() {
        return mSharedPrefs.hasPermission();
    }

    /**
     * Getter for LiveData of note files array.
     * @return the MutableLiveData stored in the ViewModel.
     */
    MutableLiveData<List<String>> getNotesListLiveData() {
        return mNotesListLiveData;
    }

    /**
     * Reads the working directory contents and builds an updated note files list.
     */
    void updateNotesList() {
        mNotesRepository.updateFilesList(mSharedPrefs.getWorkingDir(), mNotesListLiveData);
    }

    void setNightMode(int mode) {
        mSharedPrefs.setNightMode(mode);
    }

    int getNightMode() {
        return mSharedPrefs.getNightMode();
    }

    /**
     * Getter for a note name at the given position.
     * @param position a position in the notes list.
     * @return a String with the note name.
     */
    String getNoteNameAtPosition(int position) {
        return Objects.requireNonNull(mNotesListLiveData.getValue()).get(position);
    }

    /**
     * Deletes a note file with the given name.
     * @param noteName a name of a note to be deleted.
     */
    void deleteNote(String noteName) {
        File note = mDic.getFileInstance(noteName);
        mNotesRepository.deleteFile(note, mNotesListLiveData);
    }

    /**
     * Creates a note file with the given name.
     * @param noteName a name of a note to be created.
     * @return true if the file has been created successfully, otherwise - false.
     */
    boolean createNote(String noteName) {
        File note = mDic.getFileInstance(noteName);
        return mNotesRepository.createFile(note);
    }

    /**
     * Getter for the app version stored in the ViewModel, if there's none stored it builds one.
     * @return a String with the app version or "Not available" if it couldn't build one.
     */
    String getAppVersion() {
        if (mAppVersion.equals("Not available")) {
            try {
                Context context = getApplication().getApplicationContext();
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