package com.rustamft.notesft.data.storage.disk;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import com.rustamft.notesft.domain.model.Note;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Represents a note file.
 */
public class NoteData extends Note {

    private final Context context;
    private final String mWorkingDir;
    private String mName;
    private DocumentFile mFile;

    public NoteData(
            Context context,
            String workingDir,
            String noteName
    ) {
        this.context = context;
        this.mWorkingDir = workingDir;
        this.mName = noteName;
        this.mFile = buildDocumentFile(mName);
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

    public String getWorkingDir() {
        return mWorkingDir;
    }

    public String getName() {
        return mName;
    }

    protected boolean create() {
        try {
            // Get a valid parent URI
            DocumentFile parentDocument =
                    DocumentFile.fromTreeUri(context, Uri.parse(mWorkingDir));
            if (parentDocument == null) {
                return false;
            }
            Uri parentDocumentUri = parentDocument.getUri();
            // Create new file
            Uri noteFileUri = DocumentsContract.createDocument(
                    context.getContentResolver(), parentDocumentUri, "", mName
            );
            if (noteFileUri == null) {
                return false;
            }
            mFile = DocumentFile.fromSingleUri(context, noteFileUri);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected boolean delete() {
        if (mFile != null) {
            return mFile.delete();
        } else return false;
    }

    public boolean rename(String newName) {
        // Create a virtual file with entered name
        DocumentFile renamedFile = buildDocumentFile(newName);

        if (renamedFile == null) {
            return false;
        }

        if (!renamedFile.exists()) { // Do the rename and check it's successful
            if (exists()) { // If old file exists
                try {
                    Uri renamedFileUri = DocumentsContract.renameDocument(
                            context.getContentResolver(), mFile.getUri(), newName
                    );
                    renamedFile = DocumentFile.fromSingleUri(context, renamedFileUri);
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

    public boolean save(String text) {
        try {
            // Write the text to the file.
            ParcelFileDescriptor pfd =
                    context.getContentResolver().openFileDescriptor(mFile.getUri(), "wt");
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

    public String buildStringFromContent() {
        StringBuilder stringBuilder = new StringBuilder();
        try (
                InputStream inputStream =
                        context.getContentResolver().openInputStream(mFile.getUri());
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(Objects.requireNonNull(inputStream))
                )
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private DocumentFile buildDocumentFile(String fileName) {
        Uri basePathUri = Uri.parse(mWorkingDir);
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                basePathUri, DocumentsContract.getTreeDocumentId(basePathUri)
        );
        String extPath =
                childrenUri.toString().replace("/children", "%2F");
        Uri fileUri = Uri.parse(extPath + fileName);
        return DocumentFile.fromSingleUri(context, fileUri);
    }
}
