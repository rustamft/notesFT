package com.rustamft.notesft.presentation.screen.editor;

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
import com.rustamft.notesft.presentation.activity.MainActivity;
import com.rustamft.notesft.databinding.FragmentEditorBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditorFragment extends Fragment {

    private EditorViewModel mViewModel;
    private FragmentEditorBinding mBinding;
    private OnBackPressedCallback mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditorViewModel.class);
        setHasOptionsMenu(true);  // To make onCreateOptionsMenu work
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Enable ActionBar back button
        ActionBar actionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mBinding = FragmentEditorBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor, menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel); // TODO: try to avoid passing viewmodel
        // Callback to modify PopUp (back) action behavior
        mCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mViewModel.onBackPressed(mBinding.fabSave);
            }
        };
        mViewModel.registerActionBarTitleObserver((MainActivity) requireActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Modify PopUp (back) action behavior
        requireActivity().getOnBackPressedDispatcher().addCallback(mCallback);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks
        final int ACTION_RENAME_ID = R.id.action_rename;
        final int ACTION_ABOUT_NOTE_ID = R.id.action_about_note;
        switch (item.getItemId()) {
            case android.R.id.home:
                mViewModel.onBackPressed(mBinding.fabSave);
                return true;
            case ACTION_RENAME_ID:
                mViewModel.promptRename(requireView());
                return true;
            case ACTION_ABOUT_NOTE_ID:
                mViewModel.displayAboutNote(requireContext());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCallback.remove();
        mViewModel.resetActionBarTitle(requireContext());
        mBinding = null;
    }
}
