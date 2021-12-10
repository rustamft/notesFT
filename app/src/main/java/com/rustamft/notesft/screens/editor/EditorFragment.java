package com.rustamft.notesft.screens.editor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rustamft.notesft.R;
import com.rustamft.notesft.activities.MainActivity;

public class EditorFragment extends Fragment {
    public static final String NOTE_NAME = "com.rustamft.notesft.NOTE_NAME";
    EditorViewModel mEditorViewModel;
    private String mNoteName;
    OnBackPressedCallback mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // To make onCreateOptionsMenu work
        setHasOptionsMenu(true);
        // Get note name
        if (getArguments() != null) {
            mNoteName = getArguments().getString(NOTE_NAME);
        }
        // Modify PopUp (back) action behavior
        mCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this.mCallback);
        // Display ActionBar back button
        setActionBarBackEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editor, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks
        final int ACTION_RENAME_ID = R.id.action_rename;
        final int ACTION_ABOUT_NOTE_ID = R.id.action_about_note;
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case ACTION_RENAME_ID:
                promptRename();
                return true;
            case ACTION_ABOUT_NOTE_ID:
                showAboutNote();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel
        mEditorViewModel = new ViewModelProvider(this)
                .get(EditorViewModel.class);
        mEditorViewModel.setCurrentNote(mNoteName);
        // Observe toolbar title changes
        MainActivity mainActivity = ((MainActivity) requireActivity());
        mEditorViewModel.registerActionBarTitleObserver(mainActivity);
        // Register LifeCycle observer to reset toolbar title
        getLifecycle().addObserver(mEditorViewModel);
        // Initialize save FAB
        FloatingActionButton saveFAB = view.findViewById(R.id.fab_save);
        // Fill and activate EditText view
        EditText editText = view.findViewById(R.id.edittext_note);
        editText.setText(mEditorViewModel.getNoteText());
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (saveFAB.getVisibility() != View.VISIBLE) {
                    animateFade(saveFAB, 0f, 1f);
                }
            }
        };
        editText.addTextChangedListener(textWatcher);
        // Activate save FAB
        saveFAB.setOnClickListener(v -> {
            String noteText = editText.getText().toString();
            mEditorViewModel.saveTextToNote(noteText);
            animateFade(saveFAB, 1f, 0f);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // The PopUp callback is not triggered without this
        requireActivity().getOnBackPressedDispatcher().addCallback(this.mCallback);
    }

    private void animateFade(View view, float from, float to) {
        boolean fadeIn = view.getVisibility() != View.VISIBLE;
        if (fadeIn) view.setVisibility(View.VISIBLE);

        // Create and start the animation
        AlphaAnimation fadeAnimation = new AlphaAnimation(from, to);
        fadeAnimation.setDuration(500);
        view.startAnimation(fadeAnimation);

        // If fading out, hide the view after animation is ended
        if (!fadeIn) view.postDelayed(() -> view.setVisibility(View.GONE), 500);
    }

    private void unsavedTextAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.unsaved_changes)
                .setMessage(R.string.what_to_do)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    // Save button clicked
                    EditText editText = requireView().findViewById(R.id.edittext_note);
                    String noteText = editText.getText().toString();
                    mEditorViewModel.saveTextToNote(noteText);
                    navigateBack();
                })
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> {
                    // Cancel button clicked
                })
                .setNeutralButton(R.string.action_discard, (dialog, which) -> {
                    // Discard button clicked
                    navigateBack();
                });
        builder.show();
    }

    private void promptRename() {
        final View view = getLayoutInflater().inflate(R.layout.dialog_edittext, null);
        final EditText editText = view.findViewById(R.id.edittext_dialog);
        editText.setText(mEditorViewModel.getNoteName());
        // Alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.rename_note)
                .setView(view)
                .setPositiveButton(R.string.action_apply, ((dialog, which) -> {
                    // Apply button clicked
                    String newName = editText.getText().toString();
                    mEditorViewModel.renameNote(newName);
                }))
                .setNegativeButton(R.string.action_cancel, ((dialog, which) -> {
                    // Cancel button clicked
                }));
        builder.show();
    }

    private void showAboutNote() {
        String size = getString(R.string.about_note_file_size) + mEditorViewModel.getNoteSize() +
                getString(R.string.about_note_byte);
        String lastModified = getString(R.string.about_note_last_modified) +
                mEditorViewModel.getNoteLastModified();
        String path = getString(R.string.about_note_file_path) + mEditorViewModel.getNotePath();
        String message = size + "\n\n" + lastModified + "\n\n" + path;
        // Alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.about_note)
                .setMessage(message)
                .setPositiveButton(R.string.action_close, (dialog, which) -> {
                    // Close button clicked
                });
        builder.show();
    }

    /**
     * Changes the visibility of the ActionBar back button
     * @param isEnabled whether to show the back button
     */
    private void setActionBarBackEnabled(boolean isEnabled) {
        ActionBar actionBar =  ((MainActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(isEnabled);
        }
    }

    /**
     * Straight navigate back in stack without any checks
     */
    private void navigateBack() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_editorFragment_to_listFragment);
        setActionBarBackEnabled(false);
    }

    /**
     * Before navigating back checks if note has unsaved text and shows an alert if it does
     */
    private void onBackPressed() {
        FloatingActionButton saveFAB = requireView().findViewById(R.id.fab_save);
        if (saveFAB.getVisibility() == View.VISIBLE) {
            unsavedTextAlert();
        } else navigateBack();
    }
}