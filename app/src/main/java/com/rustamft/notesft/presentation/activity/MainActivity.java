package com.rustamft.notesft.presentation.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.rustamft.notesft.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onBackPressed() { // To fix Activity leak
        if (getOnBackPressedDispatcher().hasEnabledCallbacks()) {
            super.onBackPressed();
        } else {
            finishAfterTransition();
        }
    }
}
