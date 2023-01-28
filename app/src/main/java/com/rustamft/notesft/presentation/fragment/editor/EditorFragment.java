package com.rustamft.notesft.presentation.fragment.editor;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.rustamft.notesft.R;
import com.rustamft.notesft.databinding.FragmentEditorBinding;
import com.rustamft.notesft.presentation.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditorFragment extends BaseFragment<EditorViewModel, FragmentEditorBinding> {

    private OnBackPressedCallback mOnBackPressed;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        enableActionBarButton();
        enableMenu();
        initBindings();
        createBackButtonCallback();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableBackButtonCallback();
    }

    @Override
    public void onDestroyView() {
        disableBackButtonCallback();
        super.onDestroyView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_editor;
    }

    @Override
    protected EditorViewModel createViewModel() {
        return new ViewModelProvider(this).get(EditorViewModel.class);
    }

    private void enableActionBarButton() {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void createBackButtonCallback() {
        mOnBackPressed = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mViewModel.onBackPressed(mBinding.fabSave);
            }
        };
        mViewModel.registerActionBarTitleObserver((AppCompatActivity) requireActivity());
    }

    private void enableBackButtonCallback() {
        requireActivity().getOnBackPressedDispatcher().addCallback(mOnBackPressed);
    }

    private void disableBackButtonCallback() {
        mOnBackPressed.remove();
    }

    private void enableMenu() {
        requireActivity().addMenuProvider(
                new EditorMenuProvider(),
                getViewLifecycleOwner(),
                Lifecycle.State.RESUMED
        );
    }

    private void initBindings() {
        mBinding.setViewModel(mViewModel);
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
