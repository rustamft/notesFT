package com.rustamft.notesft.database;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.rustamft.notesft.R;
import com.rustamft.notesft.models.File;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NotesRepository implements Repository {

    private final Application application;

    @Inject
    public NotesRepository(Application application) {
        this.application = application;
    }

    public String getFileName(File file) {
        if (file != null) {
            return file.getName();
        } else {
            return "";
        }
    }

    public String lastModifiedAsString(File file) {
        long milliseconds = file.lastModified();
        Instant instant = Instant.ofEpochMilli(milliseconds);
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
        return dateTime.format(dateTimeFormatter);
    }

    public boolean createFile(File file) {
        if (file != null) {
            if (file.exists()) {
                displayShortToast(application.getString(R.string.same_name_note_exists));
                return false;
            } else {
                return file.create();
            }
        }
        return false;
    }

    /**
     * Reads the working directory contents and builds an array of file names.
     *
     * @param workingDir the app's working directory.
     * @return an array of strings representing file names.
     */
    private List<String> buildFilesList(String workingDir) {
        List<String> filesList = new ArrayList<>();
        String name, mime;
        ContentResolver contentResolver = application.getContentResolver();
        Uri childrenUri = buildChildrenUri(workingDir);
        try (
                Cursor c = contentResolver.query(
                        childrenUri,
                        new String[]{
                                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                                DocumentsContract.Document.COLUMN_MIME_TYPE
                        },
                        null,
                        null,
                        null
                )
        ) {
            while (c.moveToNext()) {
                name = c.getString(0);
                mime = c.getString(1);
                // Add the name to the set, if it's a file, not a dir.
                if (!(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime))) {
                    filesList.add(name);
                }
            }
        } catch (Exception e) {
            displayLongToast(e.toString());
        }
        Collections.sort(filesList);
        return filesList;
    }

    private Uri buildChildrenUri(String workingDir) {
        Uri directoryPathUri = Uri.parse(workingDir);
        return DocumentsContract.buildChildDocumentsUriUsingTree(
                directoryPathUri,
                DocumentsContract.getTreeDocumentId(directoryPathUri)
        );
    }

    /**
     * Displays a short timed toast message.
     *
     * @param message a message to be shown in the toast.
     */
    private void displayShortToast(String message) {
        Toast.makeText(application, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a long timed toast message.
     *
     * @param message a message to be shown in the toast.
     */
    private void displayLongToast(String message) {
        Toast.makeText(application, message, Toast.LENGTH_LONG).show();
    }

    /*
    /////////////////////////// Worker thread methods ///////////////////////////
    */

    public void updateFilesList(String workingDir, MutableLiveData<List<String>> filesList) {
        if (workingDir != null && filesList != null) {
            Observable<List<String>> observable = Observable.just(buildFilesList(workingDir));

            Observer<List<String>> observer = new Observer<List<String>>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull List<String> strings) {
                    filesList.postValue(strings);
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

    public void deleteFile(File file, MutableLiveData<List<String>> filesList) {
        if (file != null) {
            Observable<Boolean> observable = Observable.just(file.delete());

            Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    if (!aBoolean) {
                        displayShortToast(application.getString(R.string.could_not_do_that));
                    }
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    displayLongToast(e.toString());
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                    updateFilesList(file.getWorkingDir(), filesList);
                }
            };

            observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
    }

    public void renameFile(File file, String newName, MutableLiveData<String> actionBarTitle) {
        if (file != null) {
            Observable<Boolean> observable = Observable.just(file.rename(newName));

            Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    if (aBoolean) {
                        actionBarTitle.postValue(newName);
                    } else {
                        displayShortToast(application.getString(R.string.could_not_do_that));
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

    public void saveFile(File file, String text) {
        if (file == null) {
            displayShortToast(application.getString(R.string.could_not_do_that));
        } else {
            Observable<Boolean> observable = Observable.just(file.save(text));

            Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    if (!aBoolean) {
                        displayShortToast(application.getString(R.string.could_not_do_that));
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
}