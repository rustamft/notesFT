package com.rustamft.notesft.presentation.screen.editor;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rustamft.notesft.R;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.repository.NoteRepository;
import com.rustamft.notesft.domain.util.Constants;
import com.rustamft.notesft.domain.util.DateTimeStringBuilder;
import com.rustamft.notesft.domain.util.ToastDisplay;
import com.rustamft.notesft.presentation.activity.MainActivity;
import com.rustamft.notesft.presentation.model.ObservableNote;
import com.rustamft.notesft.presentation.navigation.Navigator;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class EditorViewModel extends ViewModel {

    private final NoteRepository mNoteRepository;
    private final ToastDisplay mToastDisplay;
    private final Navigator mNavigator;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final MutableLiveData<String> mActionBarTitle = new MutableLiveData<>();
    private androidx.lifecycle.Observer<String> mActionBarTitleObserver; // TODO: change the approach
    public final ObservableNote observableNote = new ObservableNote();

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
        String noteName = state.get(Constants.NOTE_NAME);
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
        } else navigateBack();
    }

    public void onNoteSave(EditText editText) {
        final Note note = observableNote.note.get();
        if (note == null) return;
        final Note.CopyBuilder noteCopyBuilder = note.copyBuilder();
        noteCopyBuilder.setText(editText.getText().toString());
        mDisposables.add(
                mNoteRepository.saveNote(
                                noteCopyBuilder.build()
                        )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                success -> navigateBack(),
                                error -> mToastDisplay.showLong(error.getMessage())
                        )
        );
    }

    public void onEditTextChanged(View view) {
        if (view.getVisibility() == View.GONE &&
                Boolean.TRUE.equals(observableNote.textChangedByUser.get())) {
            animateFadeIn(view);
        }
    }

    public void promptRename(View view) {
        final Note note = observableNote.note.get();
        if (note == null) return;
        final Context context = view.getContext();
        final View dialogView = View.inflate(context, R.layout.dialog_edittext, null);
        final EditText editText = dialogView.findViewById(R.id.edittext_dialog);
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
        final Note note = observableNote.note.get();
        if (note == null) return;
        String size = context.getString(R.string.about_note_file_size) + note.length() +
                context.getString(R.string.about_note_byte);
        String lastModified = context.getString(R.string.about_note_last_modified) +
                DateTimeStringBuilder.millisToString(note.lastModified());
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

    /**
     * Sets the app name to the ActionBar title.
     */
    public void resetActionBarTitle(Context context) {
        mActionBarTitle.setValue(context.getString(R.string.app_name));
        mActionBarTitle.removeObserver(mActionBarTitleObserver);
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
     * @param mainActivity the app's MainActivity.
     */
    void registerActionBarTitleObserver(MainActivity mainActivity) {
        ActionBar actionBar = mainActivity.getSupportActionBar();
        if (actionBar != null) {
            mActionBarTitleObserver = s -> actionBar.setTitle(mActionBarTitle.getValue());
            mActionBarTitle.observe(mainActivity, mActionBarTitleObserver);
        }
    }

    /**
     * Straight navigate back in stack without any checks
     */
    private void navigateBack() {
        mNavigator.popBackStack();
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
                    navigateBack();
                })
                .show();
    }

    private void animateFadeIn(View view) {
        boolean fadeIn = view.getVisibility() != View.VISIBLE;
        if (fadeIn) {
            view.setVisibility(View.VISIBLE);
        }
        AlphaAnimation fadeAnimation = new AlphaAnimation((float) 0.0, (float) 1.0);
        fadeAnimation.setDuration(500);
        view.startAnimation(fadeAnimation);
        // If fading out, hide the view after animation is ended.
        if (!fadeIn) view.postDelayed(() -> view.setVisibility(View.GONE), 500);
    }
}
