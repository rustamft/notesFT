package com.rustamft.notesft.presentation.fragment.permission;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.rustamft.notesft.R;
import com.rustamft.notesft.databinding.FragmentPermissionBinding;
import com.rustamft.notesft.presentation.base.BaseFragment;
import com.rustamft.notesft.presentation.constant.Constants;
import com.rustamft.notesft.presentation.permission.BetterActivityResult;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PermissionFragment extends BaseFragment<PermissionViewModel, FragmentPermissionBinding> {

    private BetterActivityResult<Intent, ActivityResult> mActivityLauncher;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerActivityForResult();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_permission;
    }

    @Override
    protected PermissionViewModel createViewModel() {
        return new ViewModelProvider(this).get(PermissionViewModel.class);
    }

    private void registerActivityForResult() {
        mActivityLauncher = BetterActivityResult.registerActivityForResult(this);
        Bundle arguments = getArguments();
        if (arguments != null && arguments.getBoolean(Constants.KEY_CHOOSE_WORKING_DIR_IMMEDIATELY)) {
            // If it is dir changing from the list fragment
            chooseWorkingDir();
        }
        mBinding.buttonPermission.setOnClickListener(buttonView -> chooseWorkingDir());
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
