package com.rustamft.notesft.presentation.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.rustamft.notesft.R;
import com.rustamft.notesft.presentation.navigation.Navigator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    public Navigator mNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            mNavigator.setNavController(navController);
        }
        // TODO: try "NavigationUI.setupActionBarWithNavController(this,navController)"
    }

    @Override
    public void onBackPressed() { // To fix Activity leak
        if (getOnBackPressedDispatcher().hasEnabledCallbacks()) {
            super.onBackPressed();
        } else {
            finishAfterTransition();
        }
    }

    @Override
    protected void onDestroy() {
        mNavigator.setNavController(null);
        super.onDestroy();
    }
}
