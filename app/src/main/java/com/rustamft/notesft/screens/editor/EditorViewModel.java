package com.rustamft.notesft.screens.editor;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rustamft.notesft.R;
import com.rustamft.notesft.activities.MainActivity;
import com.rustamft.notesft.database.Repository;
import com.rustamft.notesft.database.SharedPrefs;
import com.rustamft.notesft.models.File;
import com.rustamft.notesft.models.NoteFile;
import com.rustamft.notesft.utils.Constants;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditorViewModel extends ViewModel {

    private final Application application;
    private final Repository repository;
    private final MutableLiveData<String> actionBarTitle = new MutableLiveData<>();
    private androidx.lifecycle.Observer<String> actionBarTitleObserver;
    private File note;
    public String noteText;

    @Inject
    public EditorViewModel(
            Application application,
            SavedStateHandle state,
            SharedPrefs prefs,
            Repository repository
    ) {
        this.application = application;
        this.repository = repository;
        String name = state.get(Constants.NOTE_NAME);
        actionBarTitle.setValue(name);
        if (isNotNullNorBlank(name)) {
            this.note = new NoteFile(
                    application.getApplicationContext(),
                    prefs.getWorkingDir(),
                    name
            );
            this.noteText = note.buildStringFromContent();
        }
    }

    /**
     * Before navigating back checks if note has unsaved text and shows an alert if it does
     */
    public void onBackPressed(FloatingActionButton fab) {
        View view = (View) fab.getParent();
        if (fab.getVisibility() == View.VISIBLE) {
            promptUnsavedText(view);
        } else navigateBack(view);
    }

    public void onSaveFabClick(View view, EditText editText) {
        String noteText = editText.getText().toString();
        saveNoteText(noteText);
        animateFade(view, 1f, 0f);
    }

    public void onEditTextChanged(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            animateFade(view, 0f, 1f);
        }
    }

    public void promptRename(View view) {
        final Context context = view.getContext();
        final View dialogView = View.inflate(context, R.layout.dialog_edittext, null);
        final EditText editText = dialogView.findViewById(R.id.edittext_dialog);
        editText.setText(repository.getFileName(note));
        new AlertDialog.Builder(context)
                .setTitle(R.string.rename_note)
                .setView(dialogView)
                .setPositiveButton(R.string.action_apply, ((dialog, which) -> {
                    // Apply button clicked
                    String newName = editText.getText().toString();
                    repository.renameFile(note, newName, actionBarTitle);
                }))
                .setNegativeButton(R.string.action_cancel, ((dialog, which) -> {
                    // Cancel button clicked
                }))
                .show();
    }

    public void displayAboutNote(Context context) {
        String size = context.getString(R.string.about_note_file_size) + note.length() +
                context.getString(R.string.about_note_byte);
        String lastModified = context.getString(R.string.about_note_last_modified) +
                repository.lastModifiedAsString(note);
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
    public void resetActionBarTitle() {
        actionBarTitle.setValue(application.getString(R.string.app_name));
        actionBarTitle.removeObserver(actionBarTitleObserver);
    }

    boolean isNotNullNorBlank(String string) {
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
            actionBarTitleObserver = s -> actionBar.setTitle(actionBarTitle.getValue());
            actionBarTitle.observe(mainActivity, actionBarTitleObserver);
        }
    }

    /**
     * Saves the given text to the current note file.
     *
     * @param text a text to save to the current note.
     */
    void saveNoteText(String text) {
        repository.saveFile(note, text);
    }

    /**
     * Straight navigate back in stack without any checks
     */
    private void navigateBack(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.popBackStack();
    }

    private void promptUnsavedText(View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle(R.string.unsaved_changes)
                .setMessage(R.string.what_to_do)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    // Save button clicked
                    EditText editText = view.findViewById(R.id.edittext_note);
                    String noteText = editText.getText().toString();
                    saveNoteText(noteText);
                    navigateBack(view);
                })
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> {
                    // Cancel button clicked
                })
                .setNeutralButton(R.string.action_discard, (dialog, which) -> {
                    // Discard button clicked
                    navigateBack(view);
                })
                .show();
    }

    private void animateFade(View view, float from, float to) {
        boolean fadeIn = view.getVisibility() != View.VISIBLE;
        if (fadeIn) {
            view.setVisibility(View.VISIBLE);
        }
        AlphaAnimation fadeAnimation = new AlphaAnimation(from, to);
        fadeAnimation.setDuration(500);
        view.startAnimation(fadeAnimation);
        // If fading out, hide the view after animation is ended.
        if (!fadeIn) view.postDelayed(() -> view.setVisibility(View.GONE), 500);
    }
}
