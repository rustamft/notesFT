package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.storage.disk.NoteDataModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface NoteStorage {

    Boolean save(NoteDataModel note) throws IOException;

    Boolean delete(NoteDataModel note) throws IOException;

    NoteDataModel rename(NoteDataModel note, String newName) throws FileNotFoundException;

    NoteDataModel get(String noteName, String workingDir) throws NullPointerException;

    List<String> getNameList(String workingDir) throws NullPointerException;
}
