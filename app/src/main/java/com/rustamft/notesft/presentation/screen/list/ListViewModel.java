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
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.repository.NoteRepository;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.data.storage.disk.NoteData;
import com.rustamft.notesft.util.Constants;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ListViewModel extends ViewModel {

    private final Application application;
    private final AppPreferencesRepository appPreferencesRepository;
    private final NoteRepository noteRepository;
    private final MutableLiveData<List<String>> notesList = new MutableLiveData<>();
    private String appVersion = "Not available";

    @Inject
    ListViewModel(
            Application application,
            AppPreferencesRepository appPreferencesRepository,
            NoteRepository noteRepository
    ) {
        this.application = application;
        this.appPreferencesRepository = appPreferencesRepository;
        this.noteRepository = noteRepository;
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
                .setTitle(R.string.new_note)
                .setView(dialogView)
                .setPositiveButton(R.string.action_apply, ((dialog, which) -> {
                    String name = editText.getText().toString();
                    if (createNote(name)) {
                        navigateNext(view, name);
                    }
                }))
                .setNegativeButton(R.string.action_cancel, ((dialog, which) -> {
                    // Cancel.
                }))
                .show();
    }

    public void promptNavigateBack(View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle(R.string.please_confirm)
                .setMessage(R.string.are_you_sure_change_dir)
                .setPositiveButton(R.string.action_yes, (dialog, which) -> navigateBackStraightToChoosing(view))
                .setNegativeButton(R.string.action_no, (dialog, which) -> {
                    // Cancel.
                })
                .show();
    }

    public void promptDeletion(Context context, String noteName) {
        String message = context.getString(R.string.are_you_sure_delete) + " «" + noteName + "»?";
        new AlertDialog.Builder(context)
                .setTitle(R.string.please_confirm)
                .setMessage(message)
                .setPositiveButton(R.string.action_yes, (dialog, which) -> deleteNote(noteName))
                .setNegativeButton(R.string.action_no, (dialog, which) -> {
                    // Cancel.
                })
                .show();
    }

    /**
     * Checks if the app has the files read/write permission.
     *
     * @return true if the permission is granted, false otherwise.
     */
    boolean hasPermission() {
        return appPreferencesRepository.hasPermission();
    }

    /**
     * Getter for LiveData of note files array.
     *
     * @return the MutableLiveData stored in the ViewModel.
     */
    MutableLiveData<List<String>> getNotesList() {
        return notesList;
    }

    /**
     * Reads the working directory contents and builds an updated note files list.
     */
    void updateNotesList() {
        noteRepository.updateFilesList(appPreferencesRepository.getWorkingDir(), notesList);
    }

    int getNightMode() {
        return appPreferencesRepository.getNightMode();
    }

    /**
     * Getter for a note name at the given position.
     *
     * @param position a position in the notes list.
     * @return a String with the note name.
     */
    String getNoteNameAtPosition(int position) {
        return Objects.requireNonNull(notesList.getValue()).get(position);
    }

    void switchNightMode() {
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        if (nightMode != AppCompatDelegate.MODE_NIGHT_YES) {
            nightMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            nightMode = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
        appPreferencesRepository.setNightMode(nightMode);
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

    /**
     * Creates a note file with the given name.
     *
     * @param noteName a name of a note to be created.
     * @return true if the file has been created successfully, otherwise - false.
     */
    private boolean createNote(String noteName) {
        return noteRepository.createFile(
                noteName,
                appPreferencesRepository.getWorkingDir()
        );
    }

    /**
     * Deletes a note file with the given name.
     *
     * @param noteName a name of a note to be deleted.
     */
    private void deleteNote(String noteName) {
        Note note = new NoteData(
                application.getApplicationContext(),
                appPreferencesRepository.getWorkingDir(),
                noteName
        );
        noteRepository.deleteFile(note, notesList);
    }

    private void navigateBackStraightToChoosing(View view) {
        NavController navController = Navigation.findNavController(view);
        Bundle bundle = new Bundle();
        navController.navigate(R.id.action_listFragment_to_permissionFragment, bundle);
    }

    private String getAppVersion() {
        if (appVersion.equals("Not available")) {
            try {
                Context context = application.getApplicationContext();
                PackageInfo packageInfo =
                        context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                appVersion = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return appVersion;
    }

    private void openGitHub(Context context) {
        Uri webPage = Uri.parse(Constants.GITHUB_LINK);
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
}
