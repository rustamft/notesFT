package com.rustamft.notesft.presentation.screen.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rustamft.notesft.R;
import com.rustamft.notesft.databinding.FragmentListBinding;
import com.rustamft.notesft.presentation.activity.MainActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ListFragment extends Fragment {

    private ListViewModel mViewModel;
    private FragmentListBinding mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ListViewModel.class);
        setHasOptionsMenu(true); // To make onCreateOptionsMenu work
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Disable ActionBar back button
        ActionBar actionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        mBinding = FragmentListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mViewModel.hasWorkingDirPermission()) {
            mViewModel.updateNoteNameList();
        } else {
            mViewModel.navigateBack(requireView());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setAdapter(
                new NotesListAdapter(this, mViewModel)
        );
        AppCompatDelegate.setDefaultNightMode(mViewModel.getNightMode());
        registerForContextMenu(mBinding.recyclerviewList);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks
        final int ACTION_REFRESH_ID = R.id.action_refresh;
        final int ACTION_CHOOSE_DIR_ID = R.id.action_choose_dir;
        final int ACTION_SWITCH_DARK_ID = R.id.action_switch_night;
        final int ACTION_ABOUT_APP_ID = R.id.action_about_app;
        switch (item.getItemId()) {
            case ACTION_REFRESH_ID:
                View refreshAction = requireActivity().findViewById(R.id.action_refresh);
                mViewModel.animateRotation(refreshAction);
                mViewModel.updateNoteNameList();
                return true;
            case ACTION_CHOOSE_DIR_ID:
                mViewModel.promptNavigateBack(requireView());
                return true;
            case ACTION_SWITCH_DARK_ID:
                mViewModel.switchNightMode();
                return true;
            case ACTION_ABOUT_APP_ID:
                mViewModel.displayAboutApp(requireContext());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            int noteIndex = item.getGroupId();
            mViewModel.promptDeletion(requireContext(), noteIndex);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
