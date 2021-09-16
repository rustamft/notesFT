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

    boolean hasPermission() {
        return mNotesRepository.hasPermission();
    }

    MutableLiveData<String[]> getNotesListLiveData() {
        return mNotesListLiveData;
    }

    void updateNotesList() {
        mNotesRepository.updateFilesList(mNotesListLiveData);
    }

    String getNoteNameAtPosition(int position) {
        String[] notesList = mNotesListLiveData.getValue();
        if (notesList != null) {
            return notesList[position];
        } else return null;
    }

    void deleteNote(String noteName) {
        Note note = mNotesRepository.getFileInstance(noteName);
        mNotesRepository.deleteFile(note, mNotesListLiveData);
    }

    boolean createNote(String noteName) {
        Note note = mNotesRepository.getFileInstance(noteName);
        return mNotesRepository.createNewFile(note);
    }

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