package com.rustamft.notesft.domain.repository;

import androidx.lifecycle.MutableLiveData;

import com.rustamft.notesft.domain.model.Note;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface NoteRepository {

    Single<Boolean> saveNote(Note note);
    Single<Boolean> deleteNote(Note note);
    Single<Note> getNote(String noteName, String workingDir);

    /**
     * Returns a date the file was last modified.
     *
     * @param note the note file instance.
     * @return a String with the file last modified formatted date.
     */
    String lastModifiedAsString(Note note);

    /**
     * Creates a file with the given name.
     *
     * @return true if the file has been created successfully, false otherwise.
     */
    boolean createFile(String noteName, String workingDir);

    /**
     * Reads the working directory contents and builds a files list.
     *
     * @param liveDataFilesList a files list LiveData to update with the files list.
     */
    void updateFilesList(String workingDir, MutableLiveData<List<String>> liveDataFilesList);

    /**
     * Deletes a given file.
     *
     * @param note              the note file instance.
     * @param liveDataFilesList a files list LiveData to update.
     */
    void deleteFile(Note note, MutableLiveData<List<String>> liveDataFilesList);

    /**
     * Renames a given file.
     *
     * @param note                   the file instance.
     * @param newName                a new name for the file.
     * @param liveDataActionBarTitle an ActionBar title LiveData to update with the new name.
     */
    void renameFile(Note note, String newName, MutableLiveData<String> liveDataActionBarTitle);

    /**
     * Saves the given text to the file.
     *
     * @param note the note file instance.
     * @param text a text to save to the current note.
     */
    void saveFile(Note note, String text);
}
