package com.rustamft.notesft.presentation.fragment.list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.rustamft.notesft.R;
import com.rustamft.notesft.databinding.FragmentListBinding;
import com.rustamft.notesft.presentation.activity.MainActivity;
import com.rustamft.notesft.presentation.base.BaseFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ListFragment extends BaseFragment<ListViewModel, FragmentListBinding> {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        disableActionBarBackButton();
        enableMenu();
        initBindings();
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
        super.onDestroyView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_list;
    }

    @Override
    protected ListViewModel createViewModel() {
        return new ViewModelProvider(this).get(ListViewModel.class);
    }

    private void disableActionBarBackButton() {
        ActionBar actionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private void enableMenu() {
        requireActivity().addMenuProvider(
                new ListMenuProvider(),
                getViewLifecycleOwner(),
                Lifecycle.State.RESUMED
        );
    }

    private void initBindings() {
        mBinding.setViewModel(mViewModel);
        mBinding.recyclerviewList.setAdapter(mViewModel.noteListAdapter);
        String noteListFilter = mViewModel.noteList.filter.getValue();
        if (noteListFilter != null && !noteListFilter.isEmpty()) {
            mBinding.edittextSearchNote.setVisibility(View.VISIBLE);
        }
        registerForContextMenu(mBinding.recyclerviewList);
    }

    private class ListMenuProvider implements MenuProvider {

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_list, menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            final int ACTION_SEARCH_NOTE_ID = R.id.action_search_note;
            final int ACTION_CHOOSE_DIR_ID = R.id.action_choose_dir;
            final int ACTION_SWITCH_DARK_ID = R.id.action_switch_night;
            final int ACTION_ABOUT_APP_ID = R.id.action_about_app;
            switch (menuItem.getItemId()) {
                case ACTION_SEARCH_NOTE_ID:
                    EditText view = mBinding.edittextSearchNote;
                    if (view.getVisibility() == View.GONE) {
                        view.setVisibility(View.VISIBLE);
                        view.animate()
                                .translationY(0)
                                .alpha(1.0f)
                                .setDuration(300)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        view.setVisibility(View.VISIBLE);
                                    }
                                });
                    } else {
                        view.setText("");
                        view.animate()
                                .translationY(-view.getHeight())
                                .alpha(0.0f)
                                .setDuration(300)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        view.setVisibility(View.GONE);
                                    }
                                });
                    }
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
                default:
                    return false;
            }
        }
    }
}
