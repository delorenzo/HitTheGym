package com.jdelorenzo.hitthegym.backup;

import android.app.backup.FileBackupHelper;
import android.content.Context;

/*
Extension of {@link FileBackupHelper} for databases.
 */
public class DbBackupHelper extends FileBackupHelper {
    public DbBackupHelper(Context context, String name) {
        super(context, context.getDatabasePath(name).getAbsolutePath());
    }
}
