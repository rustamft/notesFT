package com.rustamft.notesft.domain.model;

import androidx.documentfile.provider.DocumentFile;

public class Note {

    public final String name;
    public final String text;
    public final String workingDir;
    private final DocumentFile mFile;

    public Note(
            String name,
            String text,
            String workingDir,
            DocumentFile file
    ) {
        this.name = name;
        this.text = text;
        this.workingDir = workingDir;
        mFile = file;
    }

    public String path() {
        return mFile.getUri().toString();
    }

    public long length() {
        return mFile.length();
    }

    public long lastModified() {
        return mFile.lastModified();
    }

    public boolean exists() {
        return mFile.exists();
    }

    public DocumentFile file() {
        return mFile;
    }

    public CopyBuilder copyBuilder() {
        return new CopyBuilder();
    }

    public class CopyBuilder {

        private String mNameCopy = name;
        private String mTextCopy = text;
        private final String mWorkingDirCopy = workingDir;
        private final DocumentFile mFileCopy = mFile;

        public void setName(String name) {
            mNameCopy = name;
        }

        public void setText(String text) {
            mTextCopy = text;
        }

        public Note build() {
            return new Note(
                    mNameCopy,
                    mTextCopy,
                    mWorkingDirCopy,
                    mFileCopy
            );
        }
    }
}
