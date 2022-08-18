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

/**
 * Represents a note file.
 */
public class NoteFile implements File {

    private final Context context;
    private final String workingDir;
    private String name;
    private DocumentFile file;

    public NoteFile(
            Context context,
            String workingDir,
            String noteName
    ) {
        this.context = context;
        this.workingDir = workingDir;
        this.name = noteName;
        this.file = buildDocumentFile(name);
    }

    public boolean exists() {
        return file.exists();
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

    public String getWorkingDir() {
        return workingDir;
    }

    public String getName() {
        return name;
    }

    public boolean create() {
        try {
            // Get a valid parent URI
            DocumentFile parentDocument =
                    DocumentFile.fromTreeUri(context, Uri.parse(workingDir));
            if (parentDocument == null) {
                return false;
            }
            Uri parentDocumentUri = parentDocument.getUri();
            // Create new file
            Uri noteFileUri = DocumentsContract.createDocument(
                    context.getContentResolver(), parentDocumentUri, "", name
            );
            if (noteFileUri == null) {
                return false;
            }
            file = DocumentFile.fromSingleUri(context, noteFileUri);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete() {
        if (file != null) {
            return file.delete();
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
                            context.getContentResolver(), file.getUri(), newName
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
                file = renamedFile;
                name = newName;
                return true;
            }
        }
        return false;
    }

    public boolean save(String text) {
        try {
            // Write the text to the file.
            ParcelFileDescriptor pfd =
                    context.getContentResolver().openFileDescriptor(file.getUri(), "wt");
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
                        context.getContentResolver().openInputStream(file.getUri());
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
        Uri basePathUri = Uri.parse(workingDir);
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                basePathUri, DocumentsContract.getTreeDocumentId(basePathUri)
        );
        String extPath =
                childrenUri.toString().replace("/children", "%2F");
        Uri fileUri = Uri.parse(extPath + fileName);
        return DocumentFile.fromSingleUri(context, fileUri);
    }
}
