package com.rustamft.notesft.domain.util;

import android.content.Context;
import android.content.UriPermission;

import java.util.List;

public class PermissionChecker { // TODO: move to presentation layer

    private final Context mContext;

    public PermissionChecker(Context context) {
        mContext = context;
    }

    public boolean hasWorkingDirPermission(String workingDir) {
        if (workingDir == null) {
            return false;
        }
        List<UriPermission> permissionsList =
                mContext.getContentResolver().getPersistedUriPermissions();
        if (permissionsList.size() != 0) {
            for (UriPermission permission : permissionsList) {
                if (permission.getUri().toString().equals(workingDir)) {
                    if (permission.isWritePermission() && permission.isReadPermission()) {
                        return true;
                    }
                }
            }
        }
        return false; // If there is no permission in list
    }
}
