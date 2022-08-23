package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.storage.disk.NoteData;

import java.io.IOException;

public interface NoteStorage {
;
    Boolean save(NoteData note) throws IOException;
    Boolean delete(NoteData note) throws IOException;
    NoteData get(String noteName, String workingDir) throws NullPointerException;
}
