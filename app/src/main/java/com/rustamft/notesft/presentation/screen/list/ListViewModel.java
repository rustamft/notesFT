package com.rustamft.notesft.presentation.screen.list;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.rustamft.notesft.BuildConfig;
import com.rustamft.notesft.R;
import com.rustamft.notesft.domain.model.AppPreferences;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.repository.NoteRepository;
import com.rustamft.notesft.domain.util.Constants;
import com.rustamft.notesft.domain.util.PermissionChecker;
import com.rustamft.notesft.domain.util.ToastDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class ListViewModel extends ViewModel {

    private final AppPreferencesRepository mAppPreferencesRepository;
    private final NoteRepository mNoteRepository;
    private final PermissionChecker mPermissionChecker;
    private final ToastDisplay mToastDisplay;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final MutableLiveData<AppPreferences> mAppPreferences = new MutableLiveData<>(
            new AppPreferences()
    );
    private final MutableLiveData<List<String>> mNoteNameList = new MutableLiveData<>(
            new ArrayList<>()
    );

    @Inject
    ListViewModel(
            AppPreferencesRepository appPreferencesRepository,
            NoteRepository noteRepository,
            PermissionChecker permissionChecker,
            ToastDisplay toastDisplay
    ) {
        mAppPreferencesRepository = appPreferencesRepository;
        mNoteRepository = noteRepository;
        mPermissionChecker = permissionChecker;
        mToastDisplay = toastDisplay;
        mDisposables.add(
                mAppPreferencesRepository.getAppPreferences().subscribe(mAppPreferences::postValue)
        );
    }

    @Override
    protected void onCleared() {
        mDisposables.clear();
        super.onCleared();
    }

    public void navigateNext(View view, String noteName) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.NOTE_NAME, noteName);
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_listFragment_to_editorFragment, bundle);
    }

    public void navigateBack(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_listFragment_to_permissionFragment);
    }

    public void promptCreation(View view) {
        final Context context = view.getContext();
        final View dialogView = View.inflate(context, R.layout.dialog_edittext, null);
        final EditText editText = dialogView.findViewById(R.id.edittext_dialog);
        new AlertDialog.Builder(context)
                .setTitle(R.string.note_new)
                .setView(dialogView)
                .setPositiveButton(R.string.action_apply, ((dialog, which) -> mDisposables.add(
                        mNoteRepository.getNote(
                                editText.getText().toString(),
                                Objects.requireNonNull(mAppPreferences.getValue()).workingDir
                        ).subscribe(
                                note -> {
                                    if (note.exists()) {
                                        mToastDisplay.showShort(R.string.note_same_name_exists);
                                    } else {
                                        createNote(view, note);
                                    }
                                },
                                error -> mToastDisplay.showLong(error.getMessage())
                        )
                )))
                .setNegativeButton(R.string.action_cancel, ((dialog, which) -> { /* Cancel */ }))
                .show();
    }

    public void promptNavigateBack(View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle(R.string.please_confirm)
                .setMessage(R.string.are_you_sure_change_dir)
                .setPositiveButton(R.string.action_yes, (dialog, which) -> navigateBackToWorkingDirChoosing(view))
                .setNegativeButton(R.string.action_no, (dialog, which) -> { /* Cancel */ })
                .show();
    }

    public void promptDeletion(Context context, int noteIndex) {
        String noteName = Objects.requireNonNull(mNoteNameList.getValue()).get(noteIndex);
        String message = context.getString(R.string.are_you_sure_delete) + " «" + noteName + "»?";
        new AlertDialog.Builder(context)
                .setTitle(R.string.please_confirm)
                .setMessage(message)
                .setPositiveButton(R.string.action_yes, (dialog, which) -> mDisposables.add(
                        mNoteRepository.getNote(
                                noteName,
                                Objects.requireNonNull(mAppPreferences.getValue()).workingDir
                        ).subscribe(
                                this::deleteNote,
                                error -> mToastDisplay.showLong(error.getMessage())
                        )
                ))
                .setNegativeButton(R.string.action_no, (dialog, which) -> { /* Cancel */ })
                .show();
    }

    protected boolean hasWorkingDirPermission() {
        return mPermissionChecker.hasWorkingDirPermission(
                Objects.requireNonNull(mAppPreferences.getValue()).workingDir
        );
    }

    MutableLiveData<List<String>> getNoteNameList() {
        return mNoteNameList;
    }

    protected void updateNoteNameList() {
        mDisposables.add(
                mNoteRepository.getNoteNameList(
                        Objects.requireNonNull(mAppPreferences.getValue()).workingDir
                ).subscribe(
                        mNoteNameList::postValue,
                        error -> mToastDisplay.showLong(error.getMessage())
                )
        );
    }

    protected int getNightMode() {
        return Objects.requireNonNull(mAppPreferences.getValue()).nightMode;
    }

    protected void switchNightMode() {
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        if (nightMode != AppCompatDelegate.MODE_NIGHT_YES) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppPreferences appPreferences = Objects.requireNonNull(mAppPreferences.getValue());
        if (appPreferences.workingDir.isEmpty()) {
            mToastDisplay.showLong(R.string.something_went_wrong);
            return;
        }
        AppPreferences.CopyBuilder appPreferencesCopyBuilder =
                mAppPreferences.getValue().copyBuilder();
        appPreferencesCopyBuilder.setNightMode(nightMode);
        AppPreferences appPreferencesCopy = appPreferencesCopyBuilder.build();
        mDisposables.add(
                mAppPreferencesRepository.saveAppPreferences(appPreferencesCopy).subscribe(
                        success -> {
                            if (success) AppCompatDelegate.setDefaultNightMode(
                                    appPreferencesCopy.nightMode
                            );
                        },
                        error -> mToastDisplay.showLong(error.getMessage())
                )
        );
    }

    protected void displayAboutApp(Context context) {
        String message =
                context.getString(R.string.about_app_content) + BuildConfig.VERSION_NAME;
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.about_app)
                .setMessage(message)
                .setPositiveButton(R.string.action_close, (dialog, which) -> {
                    // Close.
                })
                .setNegativeButton("GitHub", (dialog, which) -> {
                    // Open GitHub.
                    openGitHub(context);
                });
        builder.show();
    }

    protected void animateRotation(View view) {
        RotateAnimation rotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setInterpolator(new LinearInterpolator());
        view.startAnimation(rotate);
    }

    private void navigateBackToWorkingDirChoosing(View view) {
        NavController navController = Navigation.findNavController(view);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.CHOOSE_WORKING_DIR_IMMEDIATELY, true);
        navController.navigate(R.id.action_listFragment_to_permissionFragment, bundle);
    }

    private void createNote(View view, Note note) {
        mDisposables.add(
                mNoteRepository.saveNote(note).subscribe(
                        success -> navigateNext(view, note.name),
                        error -> mToastDisplay.showLong(error.getMessage())
                )
        );
    }

    private void deleteNote(Note note) {
        mDisposables.add(
                mNoteRepository.deleteNote(note).subscribe(
                        success -> {
                            updateNoteNameList();
                            mToastDisplay.showShort(R.string.note_deleted);
                        },
                        error -> mToastDisplay.showLong(error.getMessage())
                )
        );
    }

    private void openGitHub(Context context) {
        Uri webPage = Uri.parse(Constants.GITHUB_LINK);
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
}
