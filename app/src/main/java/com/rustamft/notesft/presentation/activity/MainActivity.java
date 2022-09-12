package com.rustamft.notesft.presentation.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.rustamft.notesft.R;
import com.rustamft.notesft.domain.repository.AppPreferencesRepository;
import com.rustamft.notesft.presentation.navigation.Navigator;
import com.rustamft.notesft.presentation.navigation.Route;
import com.rustamft.notesft.presentation.permission.PermissionChecker;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    public Navigator navigator;
    @Inject
    AppPreferencesRepository mAppPreferencesRepository;
    @Inject
    PermissionChecker mPermissionChecker;
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            navigator.setNavController(navController);
        }
        mDisposables.add(
                mAppPreferencesRepository.getAppPreferences()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                appPreferences -> {
                                    setNightMode(appPreferences.nightMode);
                                    if (!mPermissionChecker
                                            .hasWorkingDirPermission(appPreferences.workingDir)) {
                                        navigator.navigate(Route.TO_PERMISSION);
                                    }
                                }
                        )
        );
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
        navigator.setNavController(null);
        mDisposables.clear();
        super.onDestroy();
    }

    private void setNightMode(int nightMode) {
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
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
    }
}
