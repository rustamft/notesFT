package com.rustamft.notesft.screens.list;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rustamft.notesft.R;
import com.rustamft.notesft.activities.MainActivity;
import com.rustamft.notesft.screens.editor.EditorFragment;

public class ListFragment extends Fragment {
    private ListViewModel mListViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // To make onCreateOptionsMenu work
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mListViewModel.hasPermission()) {
            mListViewModel.updateNotesList();
        } else {
            navigateBack(false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        mListViewModel = new ViewModelProvider(this).get(ListViewModel.class);
        // Restore UI night mode state.
        AppCompatDelegate.setDefaultNightMode(mListViewModel.getNightMode());
        // Get LiveData reference
        LiveData<String[]> notesListLiveData = mListViewModel.getNotesListLiveData();
        // Initialize adapter
        ListAdapter adapter = new ListAdapter(getContext()) {
            @Override
            void onItemClick(String itemName) {
                navigateNext(itemName);
            }
        };
        // Observe notes list changes to update RecyclerView adapter
        notesListLiveData.observe(getViewLifecycleOwner(),
                o -> adapter.setNotesList(notesListLiveData.getValue()));
        // Initialize and fill the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        registerForContextMenu(recyclerView);
        // Initialize and activate add FAB
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> promptCreation());
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
                animateRotation(refreshAction);
                mListViewModel.updateNotesList();
                return true;
            case ACTION_CHOOSE_DIR_ID:
                promptNavigateBack();
                return true;
            case ACTION_SWITCH_DARK_ID:
                switchNightMode();
                return true;
            case ACTION_ABOUT_APP_ID:
                showAboutApp();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            int position = item.getGroupId();
            String noteName = mListViewModel.getNoteNameAtPosition(position);
            promptDeletion(noteName);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void animateRotation(View view) {
        RotateAnimation rotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setInterpolator(new LinearInterpolator());
        view.startAnimation(rotate);
    }

    private void promptNavigateBack() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.please_confirm)
                .setMessage(R.string.are_you_sure_change_dir)
                .setPositiveButton(R.string.action_yes, (dialog, which) -> {
                    // Yes button clicked
                    navigateBack(true);
                })
                .setNegativeButton(R.string.action_no, (dialog, which) -> {
                    // No button clicked
                });
        builder.show();
    }

    private void switchNightMode() {
        int mode = AppCompatDelegate.getDefaultNightMode();
        if (mode != AppCompatDelegate.MODE_NIGHT_YES) {
            mode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        }
        // Do the UI switch.
        AppCompatDelegate.setDefaultNightMode(mode);
        // Remember the choice.
        mListViewModel.setNightMode(mode);
        // Hide Up button from ActionBar.
        ActionBar actionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private void showAboutApp() {
        String message =
                getString(R.string.about_app_content) + mListViewModel.getAppVersion();
        // Alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.about_app)
                .setMessage(message)
                .setPositiveButton(R.string.action_close, (dialog, which) -> {
                    // Close button clicked
                })
                .setNegativeButton("GitHub", (dialog, which) -> {
                    // GitHub button clicked
                    openGitHub();
                });
        builder.show();
    }

    private void openGitHub() {
        Context context = requireContext();
        Uri webPage = Uri.parse("https://github.com/rustamft/notesFT");
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void promptDeletion(String noteName) {
        String message = getString(R.string.are_you_sure_delete) + " «" + noteName + "»?";
        // Alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.please_confirm)
                .setMessage(message)
                .setPositiveButton(R.string.action_yes, (dialog, which) -> {
                    // Yes button clicked
                    mListViewModel.deleteNote(noteName);
                })
                .setNegativeButton(R.string.action_no, (dialog, which) -> {
                    // No button clicked
                });
        builder.show();
    }

    private void promptCreation() {
        final View view = getLayoutInflater().inflate(R.layout.dialog_edittext, null);
        final EditText editText = view.findViewById(R.id.edittext_create);
        // Alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.new_note)
                .setView(view)
                .setPositiveButton(R.string.action_apply, ((dialog, which) -> {
                    // Apply button clicked
                    String name = editText.getText().toString();
                    if (mListViewModel.createNote(name)) {
                        navigateNext(name);
                    }
                }))
                .setNegativeButton(R.string.action_cancel, ((dialog, which) -> {
                    // Cancel button clicked
                }));
        builder.show();
    }

    private void navigateNext(String noteName) {
        Bundle bundle = new Bundle();
        bundle.putString(EditorFragment.NOTE_NAME, noteName);
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_listFragment_to_editorFragment, bundle);
    }

    private void navigateBack(boolean straightToChoosing) {
        NavController navController = NavHostFragment.findNavController(this);
        if (straightToChoosing) {
            Bundle bundle = new Bundle();
            navController.navigate(R.id.action_listFragment_to_permissionFragment, bundle);
        } else {
            navController.navigate(R.id.action_listFragment_to_permissionFragment);
        }
    }
}