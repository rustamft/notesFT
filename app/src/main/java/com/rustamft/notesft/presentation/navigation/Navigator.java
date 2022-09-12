package com.rustamft.notesft.presentation.navigation;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;

import com.rustamft.notesft.domain.Constants;

public class Navigator {

    private NavController mNavController;

    public void setNavController(NavController navController) {
        mNavController = navController;
    }

    public void navigate(Route route) {
        navigate(route, null);
    }

    public void navigate(Route route, @Nullable String noteName) {
        if (mNavController != null) {
            Bundle args = null;
            if (noteName != null) {
                args = new Bundle();
                args.putString(Constants.NOTE_NAME, noteName);
            }
            mNavController.navigate(route.resId, args);
        }
    }

    public void popBackStack() {
        mNavController.popBackStack();
    }
}
