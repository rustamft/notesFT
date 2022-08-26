package com.rustamft.notesft.domain.model;

import androidx.documentfile.provider.DocumentFile;

public abstract class Note {

    public String name;
    public String text;
    public final String workingDir;
    private final DocumentFile file;

    public Note(
            String name,
            String text,
            String workingDir,
            DocumentFile file
    ) {
        this.workingDir = workingDir;
        this.file = file;
        this.name = name;
        this.text = text;
    }

    public String path() {
        return file.getUri().toString();
    }

    public long length() {
        return file.length();
    }

    public long lastModified() {
        return file.lastModified();
    }

    public boolean exists() {
        return file.exists();
    }

    public DocumentFile file() {
        return file;
    }
}
