package com.rustamft.notesft.presentation.screen.editor;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rustamft.notesft.R;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.repository.NoteRepository;
import com.rustamft.notesft.presentation.activity.MainActivity;
import com.rustamft.notesft.util.Constants;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class EditorViewModel extends ViewModel {

    private final Application mApplication;
    private final NoteRepository mNoteRepository;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private final MutableLiveData<String> mActionBarTitle = new MutableLiveData<>();
    private androidx.lifecycle.Observer<String> mActionBarTitleObserver;
    private Note mNote;

    @Inject
    public EditorViewModel(
            Application application,
            SavedStateHandle state,
            AppPreferencesRepository appPreferencesRepository,
            NoteRepository noteRepository
    ) {
        mApplication = application;
        mNoteRepository = noteRepository;
        String noteName = state.get(Constants.NOTE_NAME);
        mActionBarTitle.setValue(noteName);
        if (isNotNullNorBlank(noteName)) {
            Disposable disposable = mNoteRepository.getNote(
                    noteName,
                    appPreferencesRepository.getWorkingDir()
            )
                    .observeOn(Schedulers.io())
                    .subscribe(
                            note -> mNote = note,
                            throwable -> displayLongToast(throwable.getMessage())
                    );
            mCompositeDisposable.add(disposable);
        }
    }

    @Override
    protected void onCleared() {
        mCompositeDisposable.dispose();
        super.onCleared();
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
        mNote.setText(editText.getText().toString());
        Disposable disposable = mNoteRepository.saveNote(mNote)
                .observeOn(Schedulers.io())
                .subscribe(
                        success -> animateFade(view, 1f, 0f),
                        throwable -> displayLongToast(throwable.getMessage())
                );
        mCompositeDisposable.add(disposable);
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
        editText.setText(mNote.getName());
        new AlertDialog.Builder(context)
                .setTitle(R.string.rename_note)
                .setView(dialogView)
                .setPositiveButton(R.string.action_apply, ((dialog, which) -> {
                    // Apply button clicked
                    String newName = editText.getText().toString();
                    mNoteRepository.renameFile(mNote, newName, mActionBarTitle);
                }))
                .setNegativeButton(R.string.action_cancel, ((dialog, which) -> {
                    // Cancel button clicked
                }))
                .show();
    }

    public void displayAboutNote(Context context) {
        String size = context.getString(R.string.about_note_file_size) + mNote.length() +
                context.getString(R.string.about_note_byte);
        String lastModified = context.getString(R.string.about_note_last_modified) +
                mNoteRepository.lastModifiedAsString(mNote);
        String path = context.getString(R.string.about_note_file_path) + mNote.path();
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
        mActionBarTitle.setValue(mApplication.getString(R.string.app_name));
        mActionBarTitle.removeObserver(mActionBarTitleObserver);
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
            mActionBarTitleObserver = s -> actionBar.setTitle(mActionBarTitle.getValue());
            mActionBarTitle.observe(mainActivity, mActionBarTitleObserver);
        }
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
                    mNote.setText(editText.getText().toString());
                    Disposable disposable = mNoteRepository.saveNote(mNote)
                            .observeOn(Schedulers.io())
                            .subscribe(
                                    success -> navigateBack(view),
                                    throwable -> displayLongToast(throwable.getMessage())
                            );
                    mCompositeDisposable.add(disposable);
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

    private void displayLongToast(String message) {
        Toast.makeText(mApplication, message, Toast.LENGTH_LONG).show();
    }
}
