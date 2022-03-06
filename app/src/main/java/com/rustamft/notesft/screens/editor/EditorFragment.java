package com.rustamft.notesft.screens.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Enable ActionBar back button.
        ActionBar actionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        binding = FragmentEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor, menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditorViewModel.class);
        binding.setViewModel(viewModel);
        // Callback to modify PopUp (back) action behavior.
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                viewModel.onBackPressed(binding.fabSave);
            }
        };
        viewModel.registerActionBarTitleObserver((MainActivity) requireActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Modify PopUp (back) action behavior.
        requireActivity().getOnBackPressedDispatcher().addCallback(callback);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks
        final int ACTION_RENAME_ID = R.id.action_rename;
        final int ACTION_ABOUT_NOTE_ID = R.id.action_about_note;
        switch (item.getItemId()) {
            case android.R.id.home:
                viewModel.onBackPressed(binding.fabSave);
                return true;
            case ACTION_RENAME_ID:
                viewModel.promptRename(requireView());
                return true;
            case ACTION_ABOUT_NOTE_ID:
                viewModel.displayAboutNote(requireContext());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        callback.remove();
        viewModel.resetActionBarTitle();
        binding = null;
    }
}