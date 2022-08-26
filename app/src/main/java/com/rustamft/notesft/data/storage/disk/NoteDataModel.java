package com.rustamft.notesft.data.storage.disk;

import androidx.documentfile.provider.DocumentFile;

import com.rustamft.notesft.domain.model.Note;

public class NoteDataModel extends Note {

    protected NoteDataModel(
            String name,
            String text,
            String workingDir,
            DocumentFile file
    ) {
        super(name, text, workingDir, file);
    }
}
