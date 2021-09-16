package com.rustamft.notesft.screens.permission;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.rustamft.notesft.R;
import com.rustamft.notesft.utils.BetterActivityResult;

public class PermissionFragment extends Fragment {
    private BetterActivityResult<Intent, ActivityResult> mActivityLauncher;
    private PermissionViewModel mPermissionViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Register this fragment to receive the requestPermission result
        mActivityLauncher = BetterActivityResult.registerActivityForResult(this);
        // Get ViewModel
        mPermissionViewModel = new ViewModelProvider(this).get(PermissionViewModel.class);
        // Arguments
        Bundle args = getArguments();
        if (args != null) { // If it is dir changing from the list fragment
            chooseWorkingDir();
        } else if (mPermissionViewModel.hasPermission()) { // If the app can read files in the dir
            navigateNext();
        }
        Button button = view.findViewById(R.id.button_permission);
        button.setOnClickListener(v -> chooseWorkingDir());
    }

    void chooseWorkingDir() {
        // Open dir choosing dialog
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        mActivityLauncher.launch(intent, result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                // Save to shared preferences and persist permission
                mPermissionViewModel.setWorkingDir(data);
                navigateNext(); // Show notes list fragment
            }
        });
    }

    private void navigateNext() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_permissionFragment_to_listFragment);
    }
}