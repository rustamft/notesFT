package com.rustamft.notesft.database;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.rustamft.notesft.R;
import com.rustamft.notesft.models.File;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NotesRepository implements Repository {
    private final Application mApplication;

    public NotesRepository(@NonNull Application application) {
        mApplication = application;
    }

    public String getFileName(File file) {
        if (file != null) {
            return file.getName();
        } else {
            return "";
        }
    }

    public String getFileText(File file) {
        String text = "";
        if (file != null) {
            try {
                text = file.getText();
            } catch (IOException e) {
                displayLongToast(e.toString());
                e.printStackTrace();
            }
        }
        return text;
    }

    public long getFileLength(File file) {
        return file.length();
    }

    public String lastModified(File file) {
        long modifiedTime = file.lastModified();
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateTimeInstance();
        return sdf.format(modifiedTime);
    }

    public boolean createNewFile(File file) {
        if (file != null) {
            if (file.exists()) {
                displayShortToast(mApplication.getString(R.string.same_name_note_exists));
                return false;
            } else {
                return file.create();
            }
        }
        return false;
    }

    /**
     * Reads the working directory contents and builds an array of file names.
     * @param workingDir the app's working directory.
     * @return an array of strings representing file names.
     */
    private String[] buildFilesArray(String workingDir) {
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

    /**
     * Displays a short timed toast message.
     * @param message a message to be shown in the toast.
     */
    private void displayShortToast(String message) {
        Toast.makeText(mApplication, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a long timed toast message.
     * @param message a message to be shown in the toast.
     */
    private void displayLongToast(String message) {
        Toast.makeText(mApplication, message, Toast.LENGTH_LONG).show();
    }

    /*
    /////////////////////////// Worker thread methods ///////////////////////////
    */

    public void updateFilesList(String workingDir, MutableLiveData<String[]> liveDataFilesList) {
        if (workingDir != null && liveDataFilesList != null) {
            Observable<String[]> observable = Observable.just(buildFilesArray(workingDir));

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

    public void deleteFile(File file, MutableLiveData<String[]> liveDataFilesList) {
        if (file != null) {
            Observable<Boolean> observable = Observable.just(file.delete());

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
                    updateFilesList(file.getWorkingDir() ,liveDataFilesList);
                }
            };

            observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
    }

    public void renameFile(File file, String newName, MutableLiveData<String> liveDataActionBarTitle) {
        if (file != null) {
            Observable<Boolean> observable = Observable.just(file.rename(newName));

            Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    if (aBoolean) {
                        liveDataActionBarTitle.postValue(newName);
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

    public void saveTextToFile(File file, String text) {
        if (file != null) {
            Observable<Boolean> observable = Observable.just(file.save(text));

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