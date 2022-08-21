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
public class NoteData extends Note { // TODO: sync fields with abstract class

    private final Context mContext;
    private final String mWorkingDir;
    private DocumentFile mFile;
    private String mName;
    private String mText = "";

    public NoteData(
            Context context,
            String workingDir,
            String noteName
    ) {
        mContext = context;
        mWorkingDir = workingDir;
        mFile = buildDocumentFile();
        mName = noteName;
        if (mFile.exists()) {
            mText = buildStringFromContent();
        }
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

    public void setText(String text) {
        mText = text;
    }

    public void setFile(DocumentFile file) {
        mFile = file;
    }

    protected DocumentFile getFile() {
        return mFile;
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

    public boolean rename(String newName) {
        // Create a virtual file with entered name
        DocumentFile renamedFile = buildDocumentFile();

        if (renamedFile == null) {
            return false;
        }

        if (!renamedFile.exists()) { // Do the rename and check it's successful
            if (mFile.exists()) { // If old file exists
                try {
                    Uri renamedFileUri = DocumentsContract.renameDocument(
                            mContext.getContentResolver(), mFile.getUri(), newName
                    );
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

    public boolean save(String text) {
        try {
            // Write the text to the file.
            ParcelFileDescriptor pfd =
                    mContext.getContentResolver().openFileDescriptor(mFile.getUri(), "wt");
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
                        mContext.getContentResolver().openInputStream(mFile.getUri());
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

    private DocumentFile buildDocumentFile() {
        Uri basePathUri = Uri.parse(mWorkingDir);
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                basePathUri,
                DocumentsContract.getTreeDocumentId(basePathUri)
        );
        String extPath = childrenUri.toString().replace("/children", "%2F");
        Uri fileUri = Uri.parse(extPath + mName);
        return DocumentFile.fromSingleUri(mContext, fileUri);
    }
}
