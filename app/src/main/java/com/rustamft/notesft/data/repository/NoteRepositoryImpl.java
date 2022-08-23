package com.rustamft.notesft.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.rustamft.notesft.R;
import com.rustamft.notesft.data.storage.NoteStorage;
import com.rustamft.notesft.data.storage.disk.NoteData;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.NoteRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NoteRepositoryImpl implements NoteRepository {

    private final Context mContext;
    private final NoteStorage mNoteStorage;

    public NoteRepositoryImpl(
            Context context,
            NoteStorage noteStorage
    ) {
        this.mContext = context;
        this.mNoteStorage = noteStorage;
    }

    public Single<Boolean> saveNote(Note note) {
        return Single.fromCallable(() -> mNoteStorage.save((NoteData) note));
    }

    public Single<Boolean> deleteNote(Note note) {
        return Single.fromCallable(() -> mNoteStorage.delete((NoteData) note));
    }

    public Single<Note> getNote(String noteName, String workingDir) {
        return Single.fromCallable(() -> mNoteStorage.get(noteName, workingDir));
    }

    public String lastModifiedAsString(Note note) {
        long milliseconds = note.lastModified();
        Instant instant = Instant.ofEpochMilli(milliseconds);
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
        return dateTime.format(dateTimeFormatter);
    }

    public boolean createFile(String noteName, String workingDir) {
        NoteData note = new NoteData(
                mContext,
                workingDir,
                noteName
        );
        if (!noteName.isEmpty()) {
            if (note.exists()) {
                displayShortToast(mContext.getString(R.string.same_name_note_exists));
                return false;
            } else {
                return mNoteStorage.save(note);
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
        ContentResolver contentResolver = mContext.getContentResolver();
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
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a long timed toast message.
     *
     * @param message a message to be shown in the toast.
     */
    private void displayLongToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    /*
    /////////////////////////// Worker thread methods ///////////////////////////
    */ // TODO: move Observer to ViewModel

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

    public void deleteFile(Note note, MutableLiveData<List<String>> filesList) {
        if (note != null) {
            Observable<Boolean> observable = Observable.just(
                    mNoteStorage.delete((NoteData) note)
            );

            Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    if (!aBoolean) {
                        displayShortToast(mContext.getString(R.string.could_not_do_that));
                    }
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    displayLongToast(e.toString());
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                    updateFilesList(note.getWorkingDir(), filesList);
                }
            };

            observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
    }

    public void renameFile(Note note, String newName, MutableLiveData<String> actionBarTitle) {
        if (note != null) {
            Observable<Boolean> observable = Observable.just(note.rename(newName));

            Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    if (aBoolean) {
                        actionBarTitle.postValue(newName);
                    } else {
                        displayShortToast(mContext.getString(R.string.could_not_do_that));
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

    public void saveFile(Note note, String text) {
        if (note == null) {
            displayShortToast(mContext.getString(R.string.could_not_do_that));
        } else {
            Observable<Boolean> observable = Observable.just(note.save(text));

            Observer<Boolean> observer = new Observer<Boolean>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                    if (!aBoolean) {
                        displayShortToast(mContext.getString(R.string.could_not_do_that));
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
