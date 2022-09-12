package com.rustamft.notesft.presentation.fragment.permission;

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

import com.rustamft.notesft.databinding.FragmentPermissionBinding;
import com.rustamft.notesft.presentation.permission.BetterActivityResult;
import com.rustamft.notesft.domain.Constants;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PermissionFragment extends Fragment {

    private PermissionViewModel mViewModel;
    private FragmentPermissionBinding mBinding;
    private BetterActivityResult<Intent, ActivityResult> mActivityLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PermissionViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        mBinding = FragmentPermissionBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Register this fragment to receive the requestPermission result
        mActivityLauncher = BetterActivityResult.registerActivityForResult(this);
        Bundle arguments = getArguments();
        if (arguments != null && arguments.getBoolean(Constants.CHOOSE_WORKING_DIR_IMMEDIATELY)) {
            // If it is dir changing from the list fragment
            chooseWorkingDir();
        }
        mBinding.buttonPermission.setOnClickListener(buttonView -> chooseWorkingDir());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void chooseWorkingDir() {
        // Open dir choosing dialog
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        mActivityLauncher.launch(intent, result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                // Save to shared preferences and persist permission
                mViewModel.saveWorkingDirPreference(data, requireView());
            }
        });
    }
}
