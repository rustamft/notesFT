package com.rustamft.notesft.presentation.fragment.list;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import com.rustamft.notesft.presentation.constant.Constants;
import com.rustamft.notesft.presentation.navigation.Navigator;
import com.rustamft.notesft.presentation.navigation.Route;
import com.rustamft.notesft.presentation.toast.ToastDisplay;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Function;

@HiltViewModel
public class ListViewModel extends ViewModel {

    public final NoteListAdapter noteNameListAdapter;
    private final AppPreferencesRepository mAppPreferencesRepository;
    private final NoteRepository mNoteRepository;
    private final ToastDisplay mToastDisplay;
    private final Navigator mNavigator;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final MutableLiveData<AppPreferences> mAppPreferencesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> mNoteNameListLiveData = new MutableLiveData<>();

    @Inject
    ListViewModel(
            AppPreferencesRepository appPreferencesRepository,
            NoteRepository noteRepository,
            ToastDisplay toastDisplay,
            Navigator navigator
    ) {
        mAppPreferencesRepository = appPreferencesRepository;
        mNoteRepository = noteRepository;
        mToastDisplay = toastDisplay;
        mNavigator = navigator;
        noteNameListAdapter = new NoteListAdapter(navigator, mNoteNameListLiveData);
        mDisposables.add(
                mAppPreferencesRepository.getAppPreferences()
                        .flatMap(appPreferences -> {
                            mAppPreferencesLiveData.postValue(appPreferences);
                            return mNoteRepository.getNoteNameList(appPreferences.workingDir);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                mNoteNameListLiveData::postValue,
                                error -> mToastDisplay.showLong(error.getMessage())
                        )
        );
    }

    @Override
    protected void onCleared() {
        mDisposables.clear();
        noteNameListAdapter.clear();
        super.onCleared();
    }

    public void promptNoteCreation(View view) {
        Context context = view.getContext();
        View dialogView = View.inflate(context, R.layout.dialog_edittext, null);
        EditText editText = dialogView.findViewById(R.id.edittext_dialog);
        new AlertDialog.Builder(context)
                .setTitle(R.string.note_new)
                .setView(dialogView)
                .setPositiveButton(R.string.action_apply, ((dialog, which) -> createNote(
                        editText.getText().toString()
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
        if (mNoteNameListLiveData.getValue() == null) return;
        List<String> noteNameList = mNoteNameListLiveData.getValue();
        String noteName = noteNameList.get(noteIndex);
        String message = context.getString(R.string.are_you_sure_delete) + " «" + noteName + "»?";
        new AlertDialog.Builder(context)
                .setTitle(R.string.please_confirm)
                .setMessage(message)
                .setPositiveButton(R.string.action_yes, (dialog, which) -> deleteNote(noteName))
                .setNegativeButton(R.string.action_no, (dialog, which) -> { /* Cancel */ })
                .show();
    }

    protected void switchNightMode() {
        AppPreferences appPreferences = mAppPreferencesLiveData.getValue();
        if (appPreferences == null || appPreferences.workingDir.isEmpty()) {
            mToastDisplay.showLong(R.string.something_went_wrong);
            return;
        }
        AppPreferences.CopyBuilder appPreferencesCopyBuilder = mAppPreferencesLiveData
                .getValue()
                .copyBuilder();
        appPreferencesCopyBuilder.setNightMode(nextNightModeBy(
                AppCompatDelegate.getDefaultNightMode()
        ));
        final AppPreferences appPreferencesCopy = appPreferencesCopyBuilder.build();
        mDisposables.add(
                mAppPreferencesRepository.saveAppPreferences(appPreferencesCopy)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                success -> {
                                    if (success) {
                                        AppCompatDelegate
                                                .setDefaultNightMode(appPreferencesCopy.nightMode);
                                    }
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

    private void navigateBackToWorkingDirChoosing(View view) {
        NavController navController = Navigation.findNavController(view);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.KEY_CHOOSE_WORKING_DIR_IMMEDIATELY, true);
        navController.navigate(R.id.action_listFragment_to_permissionFragment, bundle);
    }

    private void createNote(String noteName) {
        if (mAppPreferencesLiveData.getValue() == null) return;
        mDisposables.add(
                mNoteRepository.getNote(
                                noteName,
                                mAppPreferencesLiveData.getValue().workingDir
                        )
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap((Function<Note, SingleSource<Boolean>>) note -> {
                            if (note.exists()) {
                                mToastDisplay.showShort(R.string.note_same_name_exists);
                                return Single.just(false);
                            } else {
                                return mNoteRepository.saveNote(note);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                success -> {
                                    if (success) {
                                        mNavigator.navigate(Route.LIST_TO_EDITOR, noteName);
                                    }
                                },
                                error -> mToastDisplay.showLong(error.getMessage())
                        )
        );
    }

    private void deleteNote(String noteName) {
        if (mAppPreferencesLiveData.getValue() == null) return;
        mDisposables.add(
                mNoteRepository.getNote(
                                noteName,
                                mAppPreferencesLiveData.getValue().workingDir
                        )
                        .flatMap((Function<Note, SingleSource<Boolean>>) mNoteRepository::deleteNote)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                success -> mToastDisplay.showShort(R.string.note_deleted),
                                error -> mToastDisplay.showLong(error.getMessage())
                        )
        );
    }

    private int nextNightModeBy(int mode) {
        switch (mode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case AppCompatDelegate.MODE_NIGHT_NO:
                mToastDisplay.showShort(R.string.night_mode_auto);
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            default:
                return AppCompatDelegate.MODE_NIGHT_YES;
        }
    }

    private void openGitHub(Context context) {
        Uri webPage = Uri.parse(Constants.GITHUB_LINK);
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
}
