package com.rustamft.notesft.data.repository;

import com.rustamft.notesft.data.model.NoteDataModel;
import com.rustamft.notesft.data.storage.NoteStorage;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.NoteRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NoteRepositoryImpl implements NoteRepository {

    private final NoteStorage mNoteStorage;

    public NoteRepositoryImpl(NoteStorage noteStorage) {
        this.mNoteStorage = noteStorage;
    }

    public Completable save(Note note) {
        return mNoteStorage.save(map(note)).subscribeOn(Schedulers.io());
    }

    public Completable delete(Note note) {
        return mNoteStorage.delete(map(note)).subscribeOn(Schedulers.io());
    }

    public Single<Note> rename(Note note, String newName) {
        return mNoteStorage.rename(map(note), newName)
                .map(this::map)
                .subscribeOn(Schedulers.io());
    }

    public Single<Note> get(String noteName, String workingDir) {
        return mNoteStorage.get(noteName, workingDir)
                .map(this::map)
                .subscribeOn(Schedulers.io());
    }

    public Observable<List<String>> observeList(String workingDir) {
        return mNoteStorage.observeNameList(workingDir).subscribeOn(Schedulers.io());
    }

    private NoteDataModel map(Note note) {
        return new NoteDataModel(
                note.name,
                note.text,
                note.workingDir,
                note.file()
        );
    }

    public Note map(NoteDataModel note) {
        return new Note(
                note.name,
                note.text,
                note.workingDir,
                note.file()
        );
    }
}
