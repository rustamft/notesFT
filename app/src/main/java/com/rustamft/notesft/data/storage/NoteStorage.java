package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.model.NoteDataModel;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface NoteStorage {

    Single<Boolean> save(NoteDataModel note);

    Single<Boolean> delete(NoteDataModel note);

    Single<NoteDataModel> rename(NoteDataModel note, String newName);

    Single<NoteDataModel> get(String noteName, String workingDir);

    Observable<List<String>> getNameList(String workingDir);
}
