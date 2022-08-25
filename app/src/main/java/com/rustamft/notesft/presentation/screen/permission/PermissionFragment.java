package com.rustamft.notesft.presentation.screen.permission;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.rustamft.notesft.R;
import com.rustamft.notesft.databinding.FragmentPermissionBinding;
import com.rustamft.notesft.domain.util.BetterActivityResult;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PermissionFragment extends Fragment {

    private PermissionViewModel viewModel;
    private FragmentPermissionBinding binding;
    private BetterActivityResult<Intent, ActivityResult> activityLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PermissionViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentPermissionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setFragment(this);
        // Register this fragment to receive the requestPermission result
        activityLauncher = BetterActivityResult.registerActivityForResult(this);
        if (getArguments() != null) { // If it is dir changing from the list fragment
            chooseWorkingDir();
        } else if (viewModel.hasPermission()) { // If the app can read files in the dir
            navigateNext();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void chooseWorkingDir() {
        // Open dir choosing dialog
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        activityLauncher.launch(intent, result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                // Save to shared preferences and persist permission
                viewModel.setWorkingDir(data);
                navigateNext(); // Show notes list fragment
            }
        });
    }

    private void navigateNext() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_permissionFragment_to_listFragment);
    }
}
