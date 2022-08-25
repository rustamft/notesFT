package com.rustamft.notesft.presentation.screen.list;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import com.rustamft.notesft.R;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.repository.NoteRepository;
import com.rustamft.notesft.domain.util.Constants;
import com.rustamft.notesft.domain.util.ToastDisplay;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@HiltViewModel
public class ListViewModel extends ViewModel {

    private final Application mContext;
    private final AppPreferencesRepository mAppPreferencesRepository;
    private final NoteRepository mNoteRepository;
    private final ToastDisplay mToastDisplay;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final MutableLiveData<List<String>> mNoteNameList = new MutableLiveData<>();
    private String mAppVersion = Constants.NOT_AVAILABLE;

    @Inject
    ListViewModel(
            @ApplicationContext Application context,
            AppPreferencesRepository appPreferencesRepository,
            NoteRepository noteRepository,
            ToastDisplay toastDisplay
    ) {
        mContext = context;
        mAppPreferencesRepository = appPreferencesRepository;
        mNoteRepository = noteRepository;
        mToastDisplay = toastDisplay;
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
                                mAppPreferencesRepository.getWorkingDir()
                        ).subscribe(
                                note -> {
                                    if (note.getExists()) {
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
                                mAppPreferencesRepository.getWorkingDir()
                        ).subscribe(
                                this::deleteNote,
                                error -> mToastDisplay.showLong(error.getMessage())
                        )
                ))
                .setNegativeButton(R.string.action_no, (dialog, which) -> { /* Cancel */ })
                .show();
    }

    boolean hasWorkingDirPermission() {
        return mAppPreferencesRepository.hasWorkingDirPermission();
    }

    MutableLiveData<List<String>> getNoteNameList() {
        return mNoteNameList;
    }

    void updateNoteNameList() {
        mDisposables.add(
                mNoteRepository.getNoteNameList(
                        mAppPreferencesRepository.getWorkingDir()
                ).subscribe(
                        mNoteNameList::postValue,
                        error -> mToastDisplay.showLong(error.getMessage())
                )
        );
    }

    int getNightMode() {
        return mAppPreferencesRepository.getNightMode();
    }

    void switchNightMode() {
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        if (nightMode != AppCompatDelegate.MODE_NIGHT_YES) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
        mAppPreferencesRepository.setNightMode(nightMode);
    }

    void displayAboutApp(Context context) {
        String message =
                context.getString(R.string.about_app_content) + getAppVersion();
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

    void animateRotation(View view) {
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
        navController.navigate(R.id.action_listFragment_to_permissionFragment, bundle);
    }

    private void createNote(View view, Note note) {
        mDisposables.add(
                mNoteRepository.saveNote(note).subscribe(
                        success -> navigateNext(view, note.getName()),
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

    private String getAppVersion() { // TODO: implement through Build class
        if (mAppVersion.equals(Constants.NOT_AVAILABLE)) {
            try {
                Context context = mContext.getApplicationContext();
                PackageInfo packageInfo =
                        context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                mAppVersion = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mAppVersion;
    }

    private void openGitHub(Context context) {
        Uri webPage = Uri.parse(Constants.GITHUB_LINK);
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
}
