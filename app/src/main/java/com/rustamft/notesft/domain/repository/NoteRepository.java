package com.rustamft.notesft.domain.repository;

import com.rustamft.notesft.data.storage.disk.NoteDataModel;
import com.rustamft.notesft.domain.model.Note;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public interface NoteRepository {

    Single<Boolean> saveNote(Note note);

    Single<Boolean> deleteNote(Note note);

    Single<NoteDataModel> renameNote(Note note, String newName);

    Single<Note> getNote(String noteName, String workingDir);

    Single<List<String>> getNoteNameList(String workingDir);
}
