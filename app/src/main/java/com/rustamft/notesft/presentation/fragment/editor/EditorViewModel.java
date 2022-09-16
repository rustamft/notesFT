package com.rustamft.notesft.presentation.fragment.editor;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rustamft.notesft.R;
import com.rustamft.notesft.app.App;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.repository.NoteRepository;
import com.rustamft.notesft.presentation.constant.Constants;
import com.rustamft.notesft.presentation.model.ObservableNote;
import com.rustamft.notesft.presentation.navigation.Navigator;
import com.rustamft.notesft.presentation.time.TimeConverter;
import com.rustamft.notesft.presentation.toast.ToastDisplay;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class EditorViewModel extends ViewModel {

    public final ObservableNote observableNote = new ObservableNote();
    private final NoteRepository mNoteRepository;
    private final ToastDisplay mToastDisplay;
    private final Navigator mNavigator;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final MutableLiveData<String> mActionBarTitle = new MutableLiveData<>();

    @Inject
    public EditorViewModel(
            SavedStateHandle state,
            AppPreferencesRepository appPreferencesRepository,
            NoteRepository noteRepository,
            ToastDisplay toastDisplay,
            Navigator navigator
    ) {
        mNoteRepository = noteRepository;
        mToastDisplay = toastDisplay;
        mNavigator = navigator;
        String noteName = state.get(Constants.KEY_NOTE_NAME);
        mActionBarTitle.setValue(noteName);
        if (isNotNullNorBlank(noteName)) {
            mDisposables.add(
                    appPreferencesRepository.getAppPreferences()
                            .firstOrError()
                            .flatMap(appPreferences -> mNoteRepository.getNote(
                                    noteName,
                                    appPreferences.workingDir
                            ))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    observableNote.note::set,
                                    error -> mToastDisplay.showLong(error.getMessage())
                            )
            );
        }
    }

    @Override
    protected void onCleared() {
        resetActionBarTitle();
        mDisposables.clear();
        super.onCleared();
    }

    /**
     * Before navigating back checks if note has unsaved text and shows an alert if it does
     */
    public void onBackPressed(FloatingActionButton fab) {
        View view = (View) fab.getParent();
        if (fab.getVisibility() == View.VISIBLE) {
            promptUnsavedText(view);
        } else mNavigator.popBackStack();
    }

    public void onNoteSave(EditText editText) {
        Note note = observableNote.note.get();
        if (note == null) return;
        Note.CopyBuilder noteCopyBuilder = note.copyBuilder();
        noteCopyBuilder.setText(editText.getText().toString());
        mDisposables.add(
                mNoteRepository.saveNote(
                                noteCopyBuilder.build()
                        )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                success -> mNavigator.popBackStack(),
                                error -> mToastDisplay.showLong(error.getMessage())
                        )
        );
    }

    public void promptRename(View view) {
        Note note = observableNote.note.get();
        if (note == null) return;
        Context context = view.getContext();
        View dialogView = View.inflate(context, R.layout.dialog_edittext, null);
        EditText editText = dialogView.findViewById(R.id.edittext_dialog);
        editText.setText(note.name);
        new AlertDialog.Builder(context)
                .setTitle(R.string.note_rename)
                .setView(dialogView)
                .setPositiveButton(R.string.action_apply, ((dialog, which) -> mDisposables.add(
                        mNoteRepository.renameNote(
                                        note,
                                        editText.getText().toString()
                                )
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        resultNote -> {
                                            observableNote.note.set(resultNote);
                                            mActionBarTitle.postValue(resultNote.name);
                                        },
                                        error -> mToastDisplay.showLong(error.getMessage())
                                )
                )))
                .setNegativeButton(R.string.action_cancel, ((dialog, which) -> { /* Cancel */ }))
                .show();
    }

    public void displayAboutNote(Context context) {
        Note note = observableNote.note.get();
        if (note == null) return;
        String size = context.getString(R.string.about_note_file_size) + note.length() +
                context.getString(R.string.about_note_byte);
        String lastModified = context.getString(R.string.about_note_last_modified) +
                TimeConverter.millisToString(note.lastModified());
        String path = context.getString(R.string.about_note_file_path) + note.path();
        String message = size + "\n\n" + lastModified + "\n\n" + path;
        new AlertDialog.Builder(context)
                .setTitle(R.string.about_note)
                .setMessage(message)
                .setPositiveButton(R.string.action_close, (dialog, which) -> {
                    // Close button clicked
                })
                .show();
    }

    protected boolean isNotNullNorBlank(String string) {
        if (string == null) {
            return false;
        }
        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes the ActionBar title LiveData to be observed.
     *
     * @param activity the app's MainActivity.
     */
    protected void registerActionBarTitleObserver(AppCompatActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            String title = mActionBarTitle.getValue();
            if (title != null) {
                actionBar.setTitle(title);
            }
            mActionBarTitle.observe(activity, actionBar::setTitle);
        }
    }

    /**
     * Sets the app name to the ActionBar title.
     */
    private void resetActionBarTitle() {
        mActionBarTitle.setValue(App.NAME);
    }

    private void promptUnsavedText(View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle(R.string.unsaved_changes)
                .setMessage(R.string.what_to_do)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    // Save button clicked
                    EditText editText = view.findViewById(R.id.edittext_note);
                    onNoteSave(editText);
                })
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> {
                    // Cancel button clicked
                })
                .setNeutralButton(R.string.action_discard, (dialog, which) -> {
                    // Discard button clicked
                    mNavigator.popBackStack();
                })
                .show();
    }
}
