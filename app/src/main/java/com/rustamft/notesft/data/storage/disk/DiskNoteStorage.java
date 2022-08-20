package com.rustamft.notesft.data.storage.disk;

import com.rustamft.notesft.data.storage.NoteStorage;

public class DiskNoteStorage implements NoteStorage {

    public boolean create(NoteData note) {
        return note.create();
    }

    @Override
    public boolean delete(NoteData note) {
        return note.delete();
    }
}
