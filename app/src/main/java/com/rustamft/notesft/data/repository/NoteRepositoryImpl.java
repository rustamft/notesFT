package com.rustamft.notesft.data.repository;

import com.rustamft.notesft.data.model.NoteDataModel;
import com.rustamft.notesft.data.storage.NoteStorage;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.NoteRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NoteRepositoryImpl implements NoteRepository {

    private final NoteStorage mNoteStorage;

    public NoteRepositoryImpl(NoteStorage noteStorage) {
        this.mNoteStorage = noteStorage;
    }

    public Single<Boolean> saveNote(Note note) {
        return Single.fromCallable(
                        () -> mNoteStorage.save(
                                convertForData(note)
                        )
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> deleteNote(Note note) {
        return Single.fromCallable(
                        () -> mNoteStorage.delete(
                                convertForData(note)
                        )
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Note> renameNote(Note note, String newName) {
        return Single.fromCallable(
                        () -> mNoteStorage.rename(
                                convertForData(note),
                                newName
                        )
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::convertForDomain);
    }

    public Single<Note> getNote(String noteName, String workingDir) {
        return Single.fromCallable(
                        () -> convertForDomain(
                                mNoteStorage.get(noteName, workingDir)
                        )
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<String>> getNoteNameList(String workingDir) {
        return Single.fromCallable(
                        () -> mNoteStorage.getNameList(workingDir)
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private NoteDataModel convertForData(Note note) {
        return new NoteDataModel(
                note.name,
                note.text,
                note.workingDir,
                note.file()
        );
    }

    public Note convertForDomain(NoteDataModel note) {
        return new Note(
                note.name,
                note.text,
                note.workingDir,
                note.file()
        );
    }
}
