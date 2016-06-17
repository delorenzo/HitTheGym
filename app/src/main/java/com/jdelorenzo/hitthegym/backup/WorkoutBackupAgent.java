package com.jdelorenzo.hitthegym.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

import java.io.File;

/**
 * Extension of {@link android.app.backup.BackupAgentHelper} that backs up workout data.
 */
public class WorkoutBackupAgent extends BackupAgentHelper {
    static final String DB_NAME = "workout.db";
    static final String DB_BACKUP_KEY = "database";
    static final String PREFS_BACKUP_KEY = "preferences";
    static final String preferencesName = "pref_general";

    @Override
    public void onCreate() {
        String[] databaseList = getApplicationContext().databaseList();
        DbBackupHelper dbBackupHelper = new DbBackupHelper(this, DB_NAME);
        addHelper(DB_BACKUP_KEY, dbBackupHelper);
        SharedPreferencesBackupHelper sharedPreferencesBackupHelper
                = new SharedPreferencesBackupHelper(getApplicationContext(), preferencesName);
        addHelper(PREFS_BACKUP_KEY, sharedPreferencesBackupHelper);
    }
}
