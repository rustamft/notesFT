package com.rustamft.notesft.presentation.fragment.list;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.rustamft.notesft.BuildConfig;
import com.rustamft.notesft.R;
import com.rustamft.notesft.domain.model.AppPreferences;
import com.rustamft.notesft.domain.model.Note;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.domain.repository.NoteRepository;
import com.rustamft.notesft.presentation.base.BaseViewModel;
import com.rustamft.notesft.presentation.constant.Constants;
import com.rustamft.notesft.presentation.model.NoteList;
import com.rustamft.notesft.presentation.navigation.Navigator;
import com.rustamft.notesft.presentation.navigation.Route;
import com.rustamft.notesft.presentation.toast.ToastDisplay;

import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;

@HiltViewModel
public class ListViewModel extends BaseViewModel {

    public final NoteList noteList;
    public final NoteListAdapter noteListAdapter;
    private final MutableLiveData<AppPreferences> mAppPreferencesLiveData = new MutableLiveData<>();

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
        MutableLiveData<List<String>> listLiveData = new MutableLiveData<>();
        noteList = new NoteList(listLiveData);
        noteListAdapter = new NoteListAdapter(navigator, noteList.getFilteredLiveData());
        Disposable appPreferencesDisposable = mAppPreferencesRepository.observe()
                .switchMap(appPreferences -> {
                    mAppPreferencesLiveData.postValue(appPreferences);
                    return mNoteRepository.observeList(appPreferences.workingDir);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        listLiveData::postValue,
                        error -> mToastDisplay.showLong(error.getMessage())
                );
        disposeLater(appPreferencesDisposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        noteListAdapter.clear();
        noteList.clear();
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
        List<String> noteList = this.noteList.getFilteredLiveData().getValue();
        if (noteList == null) return;
        String noteName = noteList.get(noteIndex);
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
        Disposable appPreferencesDisposable = mAppPreferencesRepository.save(appPreferencesCopy)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> AppCompatDelegate.setDefaultNightMode(appPreferencesCopy.nightMode),
                        error -> mToastDisplay.showLong(error.getMessage())
                );
        disposeLater(appPreferencesDisposable);
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
        Disposable getNoteDisposable = mNoteRepository.get(
                        noteName,
                        mAppPreferencesLiveData.getValue().workingDir
                )
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapCompletable((Function<Note, Completable>) note -> {
                    if (note.exists()) {
                        mToastDisplay.showShort(R.string.note_same_name_exists);
                        return Completable.complete();
                    } else {
                        mNavigator.navigate(Route.LIST_TO_EDITOR, noteName);
                        return mNoteRepository.save(note);
                    }
                })
                .doOnError(error -> mToastDisplay.showLong(error.getMessage()))
                .onErrorComplete()
                .subscribe();
        disposeLater(getNoteDisposable);
    }

    private void deleteNote(String noteName) {
        if (mAppPreferencesLiveData.getValue() == null) return;
        disposeLater(
                mNoteRepository.get(
                                noteName,
                                mAppPreferencesLiveData.getValue().workingDir
                        )
                        .flatMapCompletable((Function<Note, Completable>) mNoteRepository::delete)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> mToastDisplay.showShort(R.string.note_deleted),
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
