package com.rustamft.notesft.data.storage.disk;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import com.rustamft.notesft.data.model.NoteDataModel;
import com.rustamft.notesft.data.storage.NoteStorage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NoteDiskStorage implements NoteStorage {

    private final Context mContext;

    public NoteDiskStorage(Context context) {
        this.mContext = context;
    }

    @Override
    public Boolean save(NoteDataModel note) throws IOException {
        if (note.file().exists()) {
            writeIntoExisting(note);
        } else {
            writeIntoNew(note);
        }
        return true;
    }

    @Override
    public Boolean delete(NoteDataModel note) throws IOException {
        DocumentFile file = note.file();
        if (file != null) {
            file.delete();
        } else {
            throw new IOException("File is null");
        }
        return true;
    }

    @Override
    public NoteDataModel rename(NoteDataModel note, String newName) throws FileNotFoundException {
        DocumentFile oldNameFile = note.file();
        DocumentFile newNameFile = buildDocumentFile( // Create a virtual file with entered name
                note.workingDir,
                newName
        );
        if (newNameFile == null || newNameFile.exists()) {
            throw new FileNotFoundException("New file already exists or couldn't be instantiated");
        }
        Uri renamedFileUri = DocumentsContract.renameDocument(
                mContext.getContentResolver(),
                oldNameFile.getUri(),
                newName
        );
        newNameFile = DocumentFile.fromSingleUri(
                mContext,
                renamedFileUri
        );
        if (newNameFile == null || !newNameFile.exists()) {
            throw new FileNotFoundException("Renamed file is absent or couldn't be instantiated");
        }
        return new NoteDataModel( // The note now is a new file instance, actualise note
                newName,
                note.text,
                note.workingDir,
                newNameFile
        );
    }

    @Override
    public NoteDataModel get(String noteName, String workingDir) throws NullPointerException {
        DocumentFile file = buildDocumentFile(workingDir, noteName);
        String text;
        if (file.exists()) {
            text = buildStringFromContent(file);
        } else {
            text = "";
        }
        return new NoteDataModel(
                noteName,
                text,
                workingDir,
                file
        );
    }

    @Override
    public List<String> getNameList(String workingDir) throws NullPointerException {
        List<String> filesList = new ArrayList<>();
        String name, mime;
        Cursor cursor = buildCursor(buildChildrenUri(workingDir));
        while (cursor.moveToNext()) {
            name = cursor.getString(0);
            mime = cursor.getString(1);
            // Add the name to the set, if it's a file, not a dir
            if (!(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime))) {
                filesList.add(name);
            }
        }
        cursor.close();
        Collections.sort(filesList);
        return filesList;
    }

    private DocumentFile buildDocumentFile(String workingDir, String name) {
        Uri basePathUri = Uri.parse(workingDir);
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                basePathUri,
                DocumentsContract.getTreeDocumentId(basePathUri)
        );
        String extPath = childrenUri.toString().replace("/children", "%2F");
        Uri fileUri = Uri.parse(extPath + name);
        return DocumentFile.fromSingleUri(mContext, fileUri);
    }

    private String buildStringFromContent(DocumentFile file) {
        StringBuilder stringBuilder = new StringBuilder();
        try (
                InputStream inputStream =
                        mContext.getContentResolver().openInputStream(file.getUri());
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

    private Uri buildChildrenUri(String workingDir) {
        Uri directoryPathUri = Uri.parse(workingDir);
        return DocumentsContract.buildChildDocumentsUriUsingTree(
                directoryPathUri,
                DocumentsContract.getTreeDocumentId(directoryPathUri)
        );
    }

    private Cursor buildCursor(Uri childrenUri) {
        return mContext.getContentResolver().query(
                childrenUri,
                new String[]{
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE
                },
                null,
                null,
                null
        );
    }

    private void writeIntoExisting(NoteDataModel note) throws IOException {
        // Write the text to the file
        ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(
                note.file().getUri(),
                "wt"
        );
        FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
        fos.write(note.text.getBytes());
        fos.close();
        pfd.close();
    }

    private void writeIntoNew(NoteDataModel note) throws IOException {
        // Get a valid parent URI
        DocumentFile parentDocument = DocumentFile.fromTreeUri(
                mContext,
                Uri.parse(note.workingDir)
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
                note.name
        );
        if (noteFileUri == null) {
            throw new IOException("Note file URI is null");
        }
    }
}
