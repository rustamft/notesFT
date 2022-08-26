package com.rustamft.notesft.data.repository;

import com.rustamft.notesft.data.storage.NoteStorage;
import com.rustamft.notesft.data.storage.disk.NoteDataModel;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.NoteRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class NoteRepositoryImpl implements NoteRepository {

    private final NoteStorage mNoteStorage;

    public NoteRepositoryImpl(NoteStorage noteStorage) {
        this.mNoteStorage = noteStorage;
    }

    public Single<Boolean> saveNote(Note note) {
        return Single.fromCallable(() -> mNoteStorage.save((NoteDataModel) note));
    }

    public Single<Boolean> deleteNote(Note note) {
        return Single.fromCallable(() -> mNoteStorage.delete((NoteDataModel) note));
    }

    public Single<NoteDataModel> renameNote(Note note, String newName) {
        return Single.fromCallable(() -> mNoteStorage.rename((NoteDataModel) note, newName));
    }

    public Single<Note> getNote(String noteName, String workingDir) {
        return Single.fromCallable(() -> mNoteStorage.get(noteName, workingDir));
    }

    public Single<List<String>> getNoteNameList(String workingDir) {
        return Single.fromCallable(() -> mNoteStorage.getNameList(workingDir));
    }
}
