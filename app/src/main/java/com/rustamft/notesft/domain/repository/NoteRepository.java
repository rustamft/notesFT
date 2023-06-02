package com.rustamft.notesft.domain.repository;

import com.rustamft.notesft.domain.model.Note;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface NoteRepository {

    Completable save(Note note);

    Completable delete(Note note);

    Single<Note> rename(Note note, String newName);

    Single<Note> get(String noteName, String workingDir);

    Observable<List<String>> observeList(String workingDir);
}
