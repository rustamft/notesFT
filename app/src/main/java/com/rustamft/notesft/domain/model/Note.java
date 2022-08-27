package com.rustamft.notesft.domain.model;

import androidx.documentfile.provider.DocumentFile;

public class Note {

    public final String name;
    public final String text;
    public final String workingDir;
    private final DocumentFile file;

    public Note(
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

    public CopyBuilder copyBuilder() {
        return new CopyBuilder();
    }

    public class CopyBuilder {

        private String mName = name;
        private String mText = text;
        private final String mWorkingDir = workingDir;
        private final DocumentFile mFile = file;

        public void setName(String name) {
            mName = name;
        }

        public void setText(String text) {
            mText = text;
        }

        public Note build() {
            return new Note(
                    mName,
                    mText,
                    mWorkingDir,
                    mFile
            );
        }
    }
}
