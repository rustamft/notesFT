package com.rustamft.notesft.data.storage.disk;

import androidx.documentfile.provider.DocumentFile;

import com.rustamft.notesft.domain.model.Note;

public class NoteData extends Note { // TODO: sync fields with abstract class

    private final String mWorkingDir;
    private DocumentFile mFile;
    private String mName; // TODO: check if this needs to be final
    private String mText;

    public NoteData(
            String workingDir,
            DocumentFile file,
            String name,
            String text
    ) {
        mWorkingDir = workingDir;
        mFile = file;
        mName = name;
        mText = text;
    }

    public String getWorkingDir() {
        return mWorkingDir;
    }

    public String getName() {
        return mName;
    }

    public String getText() {
        return mText;
    }

    public String getPath() {
        return mFile.getUri().toString();
    }

    public long getSize() {
        return mFile.length();
    }

    public long getLastModified() {
        return mFile.lastModified();
    }

    @Override
    public boolean getExists() {
        return mFile.exists();
    }

    public void setText(String text) {
        mText = text;
    }

    protected DocumentFile getFile() {
        return mFile;
    }

    protected void setName(String name) {
        mName = name;
    }

    protected void setFile(DocumentFile file) {
        mFile = file;
    }
}
