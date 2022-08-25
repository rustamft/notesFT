package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.storage.disk.NoteData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface NoteStorage {

    Boolean save(NoteData note) throws IOException;

    Boolean delete(NoteData note) throws IOException;

    NoteData rename(NoteData note, String newName) throws FileNotFoundException;

    NoteData get(String noteName, String workingDir) throws NullPointerException;

    List<String> getNameList(String workingDir) throws NullPointerException;
}
