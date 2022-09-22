package com.rustamft.notesft.data.repository;

import com.rustamft.notesft.data.model.NoteDataModel;
import com.rustamft.notesft.data.storage.NoteStorage;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.NoteRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NoteRepositoryImpl implements NoteRepository {

    private final NoteStorage mNoteStorage;

    public NoteRepositoryImpl(NoteStorage noteStorage) {
        this.mNoteStorage = noteStorage;
    }

    public Single<Boolean> saveNote(Note note) {
        return mNoteStorage.save(convertForData(note))
                .subscribeOn(Schedulers.io());
    }

    public Single<Boolean> deleteNote(Note note) {
        return mNoteStorage.delete(convertForData(note))
                .subscribeOn(Schedulers.io());
    }

    public Single<Note> renameNote(Note note, String newName) {
        return mNoteStorage.rename(
                        convertForData(note),
                        newName
                )
                .map(this::convertForDomain)
                .subscribeOn(Schedulers.io());
    }

    public Single<Note> getNote(String noteName, String workingDir) {
        return mNoteStorage.get(noteName, workingDir)
                .map(this::convertForDomain)
                .subscribeOn(Schedulers.io());
    }

    public Observable<List<String>> getNoteList(String workingDir) {
        return mNoteStorage.getNameList(workingDir).subscribeOn(Schedulers.io());
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
