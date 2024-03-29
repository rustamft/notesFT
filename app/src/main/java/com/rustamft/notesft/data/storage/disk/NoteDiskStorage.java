package com.rustamft.notesft.data.storage.disk;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

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
import java.util.concurrent.TimeUnit;

import androidx.documentfile.provider.DocumentFile;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class NoteDiskStorage implements NoteStorage {

    private final Context mContext;

    public NoteDiskStorage(Context context) {
        this.mContext = context;
    }

    @Override
    public Completable save(NoteDataModel note) {
        return Completable.fromCallable(() -> {
            if (note.file().exists()) {
                return writeIntoExisting(note);
            } else {
                return writeIntoNew(note);
            }
        });
    }

    @Override
    public Completable delete(NoteDataModel note) {
        return Completable.fromCallable(() -> note.file().delete());
    }

    @Override
    public Single<NoteDataModel> rename(NoteDataModel note, String newName) {
        return Single.fromCallable(() -> {
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
            return new NoteDataModel(
                    newName,
                    note.text,
                    note.workingDir,
                    newNameFile
            );
        });
    }

    @Override
    public Single<NoteDataModel> get(String noteName, String workingDir) {
        return Single.fromCallable(() -> {
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
        });
    }

    @Override
    public Observable<List<String>> observeNameList(String workingDir) {
        return Observable.interval(0, 2, TimeUnit.SECONDS)
                .map(aLong -> buildNameList(workingDir))
                .distinctUntilChanged();
    }

    private DocumentFile buildDocumentFile(String workingDir, String name) {
        String extPath = buildChildrenUri(workingDir)
                .toString()
                .replace("/children", "%2F");
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

    private Cursor buildCursor(Uri uri) {
        return mContext.getContentResolver().query(
                uri,
                new String[]{
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE
                },
                null,
                null,
                null
        );
    }

    private List<String> buildNameList(String workingDir) {
        List<String> nameList = new ArrayList<>();
        Cursor newCursor = buildCursor(buildChildrenUri(workingDir));
        String name, mime;
        while (newCursor.moveToNext()) {
            name = newCursor.getString(0);
            mime = newCursor.getString(1);
            if (!mime.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {
                nameList.add(name);
            }
        }
        newCursor.close();
        Collections.sort(nameList);
        return nameList;
    }

    private boolean writeIntoExisting(NoteDataModel note) throws IOException {
        // Write the text to the file
        ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(
                note.file().getUri(),
                "wt"
        );
        FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
        fos.write(note.text.getBytes());
        fos.close();
        pfd.close();
        return true;
    }

    private boolean writeIntoNew(NoteDataModel note) throws IOException {
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
        return true;
    }
}
