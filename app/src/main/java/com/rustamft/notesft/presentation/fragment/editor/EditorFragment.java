package com.rustamft.notesft.presentation.fragment.editor;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.rustamft.notesft.R;
import com.rustamft.notesft.databinding.FragmentEditorBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditorFragment extends Fragment {

    private EditorViewModel mViewModel;
    private FragmentEditorBinding mBinding;
    private OnBackPressedCallback mOnBackPressed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EditorViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Enable ActionBar back button
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mBinding = FragmentEditorBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.addMenuProvider(
                new EditorMenuProvider(),
                getViewLifecycleOwner(),
                Lifecycle.State.RESUMED
        );
        mBinding.setViewModel(mViewModel);
        // Callback to modify PopUp (back) action behavior
        mOnBackPressed = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mViewModel.onBackPressed(mBinding.fabSave);
            }
        };
        mViewModel.registerActionBarTitleObserver(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Modify PopUp (back) action behavior
        requireActivity().getOnBackPressedDispatcher().addCallback(mOnBackPressed);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mOnBackPressed.remove();
        mBinding = null;
    }

    private class EditorMenuProvider implements MenuProvider {

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_editor, menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            final int ACTION_RENAME_ID = R.id.action_rename;
            final int ACTION_ABOUT_NOTE_ID = R.id.action_about_note;
            switch (menuItem.getItemId()) {
                case android.R.id.home:
                    mViewModel.onBackPressed(mBinding.fabSave);
                    return true;
                case ACTION_RENAME_ID:
                    mViewModel.promptRename(requireView());
                    return true;
                case ACTION_ABOUT_NOTE_ID:
                    mViewModel.displayAboutNote(requireContext());
                    return true;
                default:
                    return false;
            }
        }
    }
}
