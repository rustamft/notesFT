package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.model.NoteDataModel;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface NoteStorage {

    Completable save(NoteDataModel note);

    Completable delete(NoteDataModel note);

    Single<NoteDataModel> rename(NoteDataModel note, String newName);

    Single<NoteDataModel> get(String noteName, String workingDir);

    Observable<List<String>> observeNameList(String workingDir);
}
