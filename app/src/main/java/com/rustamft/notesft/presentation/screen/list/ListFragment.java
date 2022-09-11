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
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getAppPreferencesLiveData().observe( // Wait for app prefs to set up
                getViewLifecycleOwner(),
                appPreferences -> {
                    setNightMode(appPreferences.nightMode);
                    if (!mViewModel.getPermissionChecker().hasWorkingDirPermission(
                            appPreferences.workingDir
                    )) {
                        mViewModel.navigateBack();
                    }
                }
        ); // TODO: doesn't set proper mode
        requireActivity().addMenuProvider(
                new ListMenuProvider(),
                getViewLifecycleOwner(),
                Lifecycle.State.RESUMED
        );
        mBinding.setViewModel(mViewModel);
        mBinding.recyclerviewList.setAdapter(mViewModel.noteNameListAdapter);
        registerForContextMenu(mBinding.recyclerviewList);
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
        mBinding.recyclerviewList.setAdapter(null);
        mBinding = null;
        super.onDestroyView();
    }

    private void setNightMode(int nightMode) {
        switch (nightMode) { // To fix IDE complains about non-constant value
            case AppCompatDelegate.MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
        }
    }

    private class ListMenuProvider implements MenuProvider {

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_list, menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            final int ACTION_CHOOSE_DIR_ID = R.id.action_choose_dir;
            final int ACTION_SWITCH_DARK_ID = R.id.action_switch_night;
            final int ACTION_ABOUT_APP_ID = R.id.action_about_app;
            switch (menuItem.getItemId()) {
                case ACTION_CHOOSE_DIR_ID:
                    mViewModel.promptNavigateBack(requireView());
                    return true;
                case ACTION_SWITCH_DARK_ID:
                    mViewModel.switchNightMode();
                    return true;
                case ACTION_ABOUT_APP_ID:
                    mViewModel.displayAboutApp(requireContext());
                    return true;
                default:
                    return false;
            }
        }
    }
}
