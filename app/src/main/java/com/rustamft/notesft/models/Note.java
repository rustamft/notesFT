package com.rustamft.notesft.models;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class Note {
    private final Context mContext;
    private final String mWorkingDir;
    private String mName;
    private DocumentFile mFile;

    public Note(Context context, String workingDir, String fileName) {
        mContext = context;
        mWorkingDir = workingDir;
        mName = fileName;
        mFile = getFileInstance(workingDir, fileName);
    }

    private DocumentFile getFileInstance(String workingDir, String fileName) {
        Uri basePathUri = Uri.parse(workingDir);
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(basePathUri,
                DocumentsContract.getTreeDocumentId(basePathUri));
        String extPath = childrenUri.toString()
                .replace("/children", "%2F");
        Uri fileUri = Uri.parse(extPath + fileName);
        return DocumentFile.fromSingleUri(mContext, fileUri);
    }

    public boolean exists() {
        return mFile.exists();
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

    public String getName() {
        return mName;
    }

    public boolean deleteFile() {
        if (mFile != null) {
            return mFile.delete();
        } else return false;
    }

    public boolean renameFile(String newName) {
        // Create a virtual file with entered name
        DocumentFile renamedFile = getFileInstance(mWorkingDir, newName);

        if (renamedFile == null) {
            return false;
        }
        boolean newFileExists = renamedFile.exists();

        if (!newFileExists) { // Do the rename and check it's successful
            if (exists()) { // If old file exists
                try {
                    Uri renamedFileUri = DocumentsContract
                            .renameDocument(mContext.getContentResolver(), mFile.getUri(), newName);
                    renamedFile = DocumentFile.fromSingleUri(mContext, renamedFileUri);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            if (renamedFile == null) {
                return false;
            }
            boolean isRenamingSuccessful = renamedFile.exists();

            if (isRenamingSuccessful) {
                // The note now is a new file instance, actualise the file variables
                mFile = renamedFile;
                mName = newName;
                return true;
            }
        }
        return false;
    }

    public boolean createNewFile() {
        try {
            // Get a valid parent URI
            DocumentFile parentDocument = DocumentFile.fromTreeUri(mContext,
                    Uri.parse(mWorkingDir));
            if (parentDocument == null) {
                return false;
            }
            Uri parentDocumentUri = parentDocument.getUri();
            // Create new file
            Uri noteFileUri = DocumentsContract.createDocument(mContext.getContentResolver(),
                    parentDocumentUri, "", mName);
            mFile = DocumentFile.fromSingleUri(mContext, noteFileUri);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean saveTextToFile(String text) {
        try {
            // Write a content of the file
            ParcelFileDescriptor pfd = mContext.getContentResolver().
                    openFileDescriptor(mFile.getUri(), "wt");
            FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
            fos.write(text.getBytes());
            fos.close();
            pfd.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getTextFromFile() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = mContext.getContentResolver().
                openInputStream(mFile.getUri());
             BufferedReader br = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        }
        // Return the note text
        return stringBuilder.toString();
    }
}