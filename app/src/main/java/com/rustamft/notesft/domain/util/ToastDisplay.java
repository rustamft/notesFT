package com.rustamft.notesft.domain.util;

import android.content.Context;
import android.widget.Toast;

public class ToastDisplay {

    private final Context mContext;

    public ToastDisplay(Context context) {
        mContext = context;
    }

    public void showShort(String message) {
        displayToast(message, Toast.LENGTH_SHORT);
    }

    public void showShort(int resId) {
        String message = mContext.getString(resId);
        displayToast(message, Toast.LENGTH_SHORT);
    }

    public void showLong(String message) {
        displayToast(message, Toast.LENGTH_LONG);
    }

    public void showLong(int resId) {
        String message = mContext.getString(resId);
        displayToast(message, Toast.LENGTH_LONG);
    }

    private void displayToast(String message, int duration) {
        Toast.makeText(mContext, message, duration).show();
    }
}
