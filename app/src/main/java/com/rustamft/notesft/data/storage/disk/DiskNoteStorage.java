package com.rustamft.notesft.data.storage.disk;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import com.rustamft.notesft.data.storage.NoteStorage;

import java.io.FileOutputStream;
import java.io.IOException;

public class DiskNoteStorage implements NoteStorage {

    private final Context mContext;

    public DiskNoteStorage(Context context) {
        this.mContext = context;
    }

    @Override
    public Boolean save(NoteData note) throws IOException {
        if (note.getFile().exists()) {
            updateExisting(note);
        } else {
            createNew(note);
        }
        return true;
    }

    @Override
    public Boolean delete(NoteData note) throws IOException {
        DocumentFile file = note.getFile();
        if (file != null) {
            file.delete();
        } else {
            throw new IOException("File is null");
        }
        return true;
    }

    @Override
    public NoteData get(String noteName, String workingDir) throws NullPointerException {
        return new NoteData(
                mContext,
                workingDir,
                noteName
        );
    }

    private void updateExisting(NoteData note) throws IOException {
        // Write the text to the file
        ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(
                note.getFile().getUri(),
                "wt"
        );
        FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
        fos.write(note.getText().getBytes());
        fos.close();
        pfd.close();
    }

    private void createNew(NoteData note) throws IOException {
        // Get a valid parent URI
        DocumentFile parentDocument = DocumentFile.fromTreeUri(
                mContext,
                Uri.parse(note.getWorkingDir())
        );
        if (parentDocument == null) {
            throw new IOException("Parent document is null");
        }
        Uri parentDocumentUri = parentDocument.getUri();
        // Create new file
        Uri noteFileUri = DocumentsContract.createDocument(
                mContext.getContentResolver(),
                parentDocumentUri,
                "",
                note.getName()
        );
        if (noteFileUri == null) {
            throw new IOException("Note file URI is null");
        }
        note.setFile(DocumentFile.fromSingleUri(mContext, noteFileUri));
    }
}
