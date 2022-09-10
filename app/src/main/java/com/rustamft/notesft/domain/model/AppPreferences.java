package com.rustamft.notesft.domain.model;

public class AppPreferences {

    public final int nightMode;
    public final String workingDir;

    public AppPreferences(
            int nightMode,
            String workingDir
    ) {
        this.nightMode = nightMode;
        this.workingDir = workingDir;
    }

    public CopyBuilder copyBuilder() {
        return new CopyBuilder();
    }

    public class CopyBuilder {

        private int mNightModeCopy = nightMode;
        private String mWorkingDirCopy = workingDir;

        public void setNightMode(int nightMode) {
            mNightModeCopy = nightMode;
        }

        public void setWorkingDir(String workingDir) {
            mWorkingDirCopy = workingDir;
        }

        public AppPreferences build() {
            return new AppPreferences(
                    mNightModeCopy,
                    mWorkingDirCopy
            );
        }
    }
}
