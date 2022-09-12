package com.rustamft.notesft.presentation.navigation;

import androidx.annotation.IdRes;

import com.rustamft.notesft.R;

public enum Route {

    PERMISSION_TO_LIST(R.id.action_permissionFragment_to_listFragment),
    LIST_TO_EDITOR(R.id.action_listFragment_to_editorFragment),
    TO_PERMISSION(R.id.permissionFragment);

    public final int resId;

    Route(@IdRes int resId) {
        this.resId = resId;
    }
}
