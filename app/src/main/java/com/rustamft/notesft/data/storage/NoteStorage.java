package com.rustamft.notesft.data.storage;

import com.rustamft.notesft.data.storage.disk.NoteData;

public interface NoteStorage {
;
    boolean create(NoteData note);
    boolean delete(NoteData note);
}
