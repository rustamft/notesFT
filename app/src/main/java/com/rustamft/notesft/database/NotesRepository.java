package com.rustamft.notesft.database;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.rustamft.notesft.R;
import com.rustamft.notesft.models.Note;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NotesRepository {
    private static NotesRepository INSTANCE;
    private final String SHARED_PREF_FILE = "com.rustamft.notesft.shared_preferences";
    private final String WORKING_DIR_KEY = "working_dir";
    private final Application mApplication;

    private NotesRepository(@NonNull Application application) {
        mApplication = application;
    }

    public static NotesRepository getInstance(Application application) {
        if(INSTANCE == null) {
            INSTANCE = new NotesRepository(application);
        }
        return INSTANCE;
    }

    public boolean hasPermission() {
        // Get working dir
        String workingDir = getWorkingDir();
        // Check if there is saved workingDir in shared preferences
        if (workingDir == null) {
            return false;
        }
        // Check permission
        List<UriPermission> permissionsList = mApplication.getContentResolver()
                .getPersistedUriPermissions();

        if (permissionsList.size() != 0) {
            for (UriPermission permission : permissionsList) {
                if (permission.getUri().toString().equals(workingDir)) {
                    if (permission.isWritePermission() && permission.isReadPermission()) {
                        return true;
                    }
                }
            }
        }
        // If there is no permission
        return false;
    }

    @SuppressLint("WrongConstant")
    public void setWorkingDir(Intent resultData) {
        Uri directoryUri = resultData.getData();
        final int flags = resultData.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        mApplication.getContentResolver().takePersistableUriPermission(directoryUri, flags);

        String workingDir = directoryUri.toString();
        SharedPreferences sharedPreferences = mApplication
                .getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(WORKING_DIR_KEY, workingDir);
        preferencesEditor.apply();
    }

    private String getWorkingDir() {
        SharedPreferences sharedPreferences = mApplication
                .getSharedPreferences(SHARED_PREF_FILE, MODE_PRIVATE);
        return sharedPreferences.getString(WORKING_DIR_KEY, null);
    }

    public Note getFileInstance(String name) {
        // Check if the name is valid: has at least one letter or digit
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if (Character.isLetterOrDigit(c)) { // If the name is valid
                String workingDir = getWorkingDir();
                return new Note(mApplication, workingDir, name); // The cycle stopped
            }
        }
        return null; // If the name is invalid
    }

    public String getFileName(Note file) {
        if (file != null) {
            return file.getName();
        } else {
            return "";
        }
    }

    public String getFileText(Note file) {
        String text = "";
        if (file != null) {
            try {
                text = file.getTextFromFile();
            } catch (IOException e) {
                displayLongToast(e.toString());
                e.printStackTrace();
            }
        }
        return text;
    }

    public long getFileLength(Note file) {
        return file.length();
    }

    public String lastModified(Note note) {
        long modifiedTime = note.lastModified();
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateTimeInstance();
        return sdf.format(modifiedTime);
    }

    public boolean createNewFile(Note file) {
        if (file != null) {
            if (file.exists()) {
                displayShortToast(mApplication.getString(R.string.same_name_note_exists));
                return false;
            } else {
                return file.createNewFile();
            }
        }
        return false;
    }

    private String[] getFilesArray(String workingDir) {
        // Notes names array to be returned
        String[] filesArray;
        // Build directory children URI
        Uri directoryPathUri = Uri.parse(workingDir);
        ContentResolver contentResolver = mApplication.getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(directoryPathUri,
                DocumentsContract.getTreeDocumentId(directoryPathUri));
        // Create a set to remember notes names
        HashSet<String> filesSet = new HashSet<>();
        // Variables to remember single note name and mime type
        String name, mime;
        // Go through notes
        try (Cursor c = contentResolver.query(childrenUri, new String[]{
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE},
                null, null, null)) {
            while (c.moveToNext()) {
                name = c.getString(0);
                mime = c.getString(1);
                // Add the name to the set, if it's a file
                if (!(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime))) {
                    filesSet.add(name);
                }
            }
        } catch (Exception e) {
            displayLongToast(e.toString());
        }
        // Put the set to an array and sort it
        filesArray = new String[filesSet.size()];
        int i = 0;
        for (String fileName : filesSet) {
            filesArray[i] = fileName;
            i++;
        }
        Arrays.sort(filesArray);
        return filesArray;
    }

    public void displayShortToast(String message) {
        Toast.makeText(mApplication, message, Toast.LENGTH_SHORT).show();
    }

    public void displayLongToast(String message) {
        Toast.makeText(mApplication, message, Toast.LENGTH_LONG).show();
    }

    /*
    /////////////////////////// Worker thread methods ///////////////////////////
    */

    public void updateFilesList(MutableLiveData<String[]> liveDataFilesList) {
        // Get working dir
        String workingDir = getWorkingDir();
        if (workingDir != null && liveDataFilesList != null) {
            Observable<String[]> observable = Observable.just(getFilesArray(workingDir));

            Observer<String[]> observer = new Observer<String[]>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(String @io.reactivex.rxjava3.annotations.NonNull [] strings) {
                    liveDataFilesList.postValue(strings);
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    displayLongToast(e.toString());
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                }
            };

            observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
    }

    public void deleteFile(Note file, MutableLiveData<String[]> liveDataFilesList) {
        if (file != null) {
            Observable<Boolean> observable = Observable.just(file.deleteFile());

            Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    if (!aBoolean) {
                        displayShortToast(mApplication.getString(R.string.could_not_do_that));
                    }
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    displayLongToast(e.toString());
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                    updateFilesList(liveDataFilesList);
                }
            };

            observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
    }

    public void renameFile(Note file, String newName, MutableLiveData<String> liveDataToolbarTitle) {
        if (file != null) {
            Observable<Boolean> observable = Observable.just(file.renameFile(newName));

            Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    if (aBoolean) {
                        liveDataToolbarTitle.postValue(newName);
                    } else {
                        displayShortToast(mApplication.getString(R.string.could_not_do_that));
                    }
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    displayLongToast(e.toString());
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                }
            };

            observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
    }

    public void saveTextToFile(Note file, String text) {
        if (file != null) {
            Observable<Boolean> observable = Observable.just(file.saveTextToFile(text));

            Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    if (!aBoolean) {
                        displayShortToast(mApplication.getString(R.string.could_not_do_that));
                    }
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    displayLongToast(e.toString());
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                }
            };

            observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        } else {
            displayShortToast(mApplication.getString(R.string.could_not_do_that));
        }
    }
}