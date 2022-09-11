package com.rustamft.notesft.presentation.navigation;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;

public class Navigator { // TODO: try to make destination choosing easier

    private NavController mNavController;

    public void setNavController(NavController navController) {
        mNavController = navController;
    }

    public void navigate(@IdRes int resId) {
        navigate(resId, null);
    }

    public void navigate(@IdRes int resId, @Nullable Bundle args) {
        if (mNavController != null) {
            mNavController.navigate(resId, args);
        }
    }

    public void popBackStack() {
        mNavController.popBackStack();
    }
}
