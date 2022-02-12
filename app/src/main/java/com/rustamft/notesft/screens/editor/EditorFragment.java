package com.rustamft.notesft.screens.editor;

import android.os.Bundle;
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
import com.rustamft.notesft.databinding.FragmentEditorBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditorFragment extends Fragment {

    private EditorViewModel viewModel;
    private FragmentEditorBinding binding;
    OnBackPressedCallback callback;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // To make onCreateOptionsMenu work
        setHasOptionsMenu(true);
        // Modify PopUp (back) action behavior
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this.callback);
        // Display ActionBar back button
        setActionBarBackEnabled(true);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
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
                displayAboutNote();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // The PopUp callback is not triggered without this.
        requireActivity().getOnBackPressedDispatcher().addCallback(this.callback);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditorViewModel.class);
        binding.setFragment(this);
        binding.setViewModel(viewModel);
        viewModel.registerActionBarTitleObserver((MainActivity) requireActivity());
        // Register LifeCycle observer to reset toolbar title.
        getLifecycle().addObserver(viewModel);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onSaveFabClick() {
        String noteText = binding.edittextNote.getText().toString();
        viewModel.saveNoteText(noteText);
        animateFade(binding.fabSave, 1f, 0f);
    }

    public void onEditTextChanged() {
        if (binding.fabSave.getVisibility() != View.VISIBLE) {
            animateFade(binding.fabSave, 0f, 1f);
        }
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

    private void promptUnsavedText() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.unsaved_changes)
                .setMessage(R.string.what_to_do)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    // Save button clicked
                    EditText editText = requireView().findViewById(R.id.edittext_note);
                    String noteText = editText.getText().toString();
                    viewModel.saveNoteText(noteText);
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
        editText.setText(viewModel.getNoteName());
        // Alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.rename_note)
                .setView(view)
                .setPositiveButton(R.string.action_apply, ((dialog, which) -> {
                    // Apply button clicked
                    String newName = editText.getText().toString();
                    viewModel.renameNote(newName);
                }))
                .setNegativeButton(R.string.action_cancel, ((dialog, which) -> {
                    // Cancel button clicked
                }));
        builder.show();
    }

    private void displayAboutNote() {
        String size = getString(R.string.about_note_file_size) + viewModel.getNoteSize() +
                getString(R.string.about_note_byte);
        String lastModified = getString(R.string.about_note_last_modified) +
                viewModel.getNoteLastModified();
        String path = getString(R.string.about_note_file_path) + viewModel.getNotePath();
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
            promptUnsavedText();
        } else navigateBack();
    }
}