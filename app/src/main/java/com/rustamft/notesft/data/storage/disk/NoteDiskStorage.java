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
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class NoteDiskStorage implements NoteStorage {

    private final Context mContext;

    public NoteDiskStorage(Context context) {
        this.mContext = context;
    }

    @Override
    public Single<Boolean> save(NoteDataModel note) {
        return Single.create(emitter -> {
                    if (!emitter.isDisposed()) {
                        if (note.file().exists()) {
                            emitter.onSuccess(writeIntoExisting(note));
                        } else {
                            emitter.onSuccess(writeIntoNew(note));
                        }
                    }
                }
        );
    }

    @Override
    public Single<Boolean> delete(NoteDataModel note) {
        return Single.create(emitter -> {
            DocumentFile file = note.file();
            if (!emitter.isDisposed()) {
                if (file != null) {
                    emitter.onSuccess(file.delete());
                } else {
                    emitter.onError(new IOException("File is null"));
                }
            }
        });
    }

    @Override
    public Single<NoteDataModel> rename(NoteDataModel note, String newName) {
        return Single.create(emitter -> {
            DocumentFile oldNameFile = note.file();
            DocumentFile newNameFile = buildDocumentFile( // Create a virtual file with entered name
                    note.workingDir,
                    newName
            );
            if (!emitter.isDisposed() && (newNameFile == null || newNameFile.exists())) {
                emitter.onError(
                        new FileNotFoundException(
                                "New file already exists or couldn't be instantiated"
                        )
                );
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
            if (!emitter.isDisposed() && (newNameFile == null || !newNameFile.exists())) {
                emitter.onError(
                        new FileNotFoundException(
                                "Renamed file is absent or couldn't be instantiated"
                        )
                );
            }
            if (!emitter.isDisposed()) {
                emitter.onSuccess(
                        new NoteDataModel(
                                newName,
                                note.text,
                                note.workingDir,
                                newNameFile
                        )
                );
            }
        });
    }

    @Override
    public Single<NoteDataModel> get(String noteName, String workingDir) {
        return Single.create(emitter -> {
            DocumentFile file = buildDocumentFile(workingDir, noteName);
            String text;
            if (file.exists()) {
                text = buildStringFromContent(file);
            } else {
                text = "";
            }
            if (!emitter.isDisposed()) {
                emitter.onSuccess(
                        new NoteDataModel(
                                noteName,
                                text,
                                workingDir,
                                file
                        )
                );
            }
        });
    }

    @Override
    public Observable<List<String>> getNameList(String workingDir) {
        List<String> fileList = new ArrayList<>();
        return Observable.interval(1, TimeUnit.SECONDS)
                .flatMap(aLong -> {
                    List<String> newFileList = new ArrayList<>();
                    Cursor newCursor = buildCursor(buildChildrenUri(workingDir));
                    String name, mime;
                    while (newCursor.moveToNext()) {
                        name = newCursor.getString(0);
                        mime = newCursor.getString(1);
                        if (!mime.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {
                            newFileList.add(name);
                        }
                    }
                    newCursor.close();
                    Collections.sort(newFileList);
                    if (newFileList.equals(fileList)) {
                        return Observable.empty();
                    } else {
                        fileList.clear();
                        fileList.addAll(newFileList);
                        return Observable.just(newFileList);
                    }
                });
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
