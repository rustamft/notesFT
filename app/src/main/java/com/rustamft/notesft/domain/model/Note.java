package com.rustamft.notesft.domain.model;

import androidx.documentfile.provider.DocumentFile;

public abstract class Note {

    public abstract String getWorkingDir();
    public abstract String getName();
    public abstract String getText();
    public abstract String getPath();
    public abstract long getSize();
    public abstract long getLastModified();
    public abstract boolean getExists();
    public abstract void setText(String text);

    protected abstract DocumentFile getFile();
    protected abstract void setName(String name);
    protected abstract void setFile(DocumentFile file);
}
