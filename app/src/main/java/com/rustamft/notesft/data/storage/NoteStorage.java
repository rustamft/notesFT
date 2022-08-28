package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.model.NoteDataModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface NoteStorage {

    boolean save(NoteDataModel note) throws IOException;

    boolean delete(NoteDataModel note) throws IOException;

    NoteDataModel rename(NoteDataModel note, String newName) throws FileNotFoundException;

    NoteDataModel get(String noteName, String workingDir) throws NullPointerException;

    List<String> getNameList(String workingDir) throws NullPointerException;
}
