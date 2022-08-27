package com.rustamft.notesft.data.model;

import androidx.documentfile.provider.DocumentFile;

public class NoteDataModel {

    public final String name;
    public final String text;
    public final String workingDir;
    private final DocumentFile file;

    public NoteDataModel(
            String name,
            String text,
            String workingDir,
            DocumentFile file
    ) {
        this.name = name;
        this.text = text;
        this.workingDir = workingDir;
        this.file = file;
    }

    public DocumentFile file() {
        return file;
    }
}
