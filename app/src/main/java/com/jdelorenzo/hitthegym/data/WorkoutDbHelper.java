package com.jdelorenzo.hitthegym.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jdelorenzo.hitthegym.data.WorkoutContract.*;
import com.jdelorenzo.hitthegym.model.Exercise;

public class WorkoutDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "workout.db";

    public WorkoutDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static final String SQL_CREATE_ROUTINE_TABLE = "CREATE TABLE " +
            RoutineEntry.TABLE_NAME + " (" +
            RoutineEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            RoutineEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            RoutineEntry.COLUMN_LAST_MODIFIED + " TEXT" +
            " );";

    static final String SQL_CREATE_DAY_TABLE = "CREATE TABLE " +
            DayEntry.TABLE_NAME + " ( " +
            DayEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DayEntry.COLUMN_ROUTINE_KEY + " INTEGER NOT NULL, " +
            DayEntry.COLUMN_DAY_OF_WEEK + " INTEGER NOT NULL, " +
            DayEntry.COLUMN_LAST_DATE + " TEXT, " +
            "FOREIGN KEY (" + DayEntry.COLUMN_ROUTINE_KEY + ") REFERENCES " +
            RoutineEntry.TABLE_NAME + " (" + RoutineEntry._ID + ") " +
            "ON DELETE CASCADE ON UPDATE CASCADE " +
            " );";

    static final String SQL_CREATE_EXERCISE_TABLE = "CREATE TABLE " +
            ExerciseEntry.TABLE_NAME + " ( " +
            ExerciseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ExerciseEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
            ExerciseEntry.COLUMN_REPS + " INTEGER, " +
            ExerciseEntry.COLUMN_SETS + " INTEGER, " +
            ExerciseEntry.COLUMN_MEASUREMENT + " REAL, " +
            ExerciseEntry.COLUMN_MEASUREMENT_TYPE + " INTEGER " +
            " );";

    static final String SQL_CREATE_PROGRESS_TABLE = "CREATE TABLE " +
            ProgressEntry.TABLE_NAME + " ( " +
            ProgressEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ProgressEntry.COLUMN_EXERCISE_KEY + " INTEGER NOT NULL, " +
            ProgressEntry.COLUMN_MEASUREMENT + " REAL, " +
            ProgressEntry.COLUMN_DATE + " TEXT, " +
            "FOREIGN KEY (" + ProgressEntry.COLUMN_EXERCISE_KEY + ") REFERENCES " +
            ExerciseEntry.TABLE_NAME + " (" + ExerciseEntry._ID + ") " +
            "ON DELETE CASCADE ON UPDATE CASCADE);";

    static final String SQL_CREATE_EXERCISE_DAY_LINKER_TABLE = "CREATE TABLE " +
            ExerciseDayLinkerEntry.TABLE_NAME + " ( " +
            ExerciseDayLinkerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ExerciseDayLinkerEntry.COLUMN_DAY_KEY + " INTEGER NOT NULL, " +
            ExerciseDayLinkerEntry.COLUMN_EXERCISE_KEY + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + ExerciseDayLinkerEntry.COLUMN_DAY_KEY +") REFERENCES " +
            DayEntry.TABLE_NAME + " (" + DayEntry._ID + ") " +
            "ON DELETE CASCADE ON UPDATE CASCADE " +
            "FOREIGN KEY (" + ExerciseDayLinkerEntry.COLUMN_EXERCISE_KEY +") REFERENCES " +
            ExerciseEntry.TABLE_NAME + "(" + ExerciseEntry._ID + ") " +
            "ON DELETE CASCADE ON UPDATE CASCADE);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ROUTINE_TABLE);
        db.execSQL(SQL_CREATE_DAY_TABLE);
        db.execSQL(SQL_CREATE_EXERCISE_TABLE);
        db.execSQL(SQL_CREATE_PROGRESS_TABLE);
        db.execSQL(SQL_CREATE_EXERCISE_DAY_LINKER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
                    //From version 1 to 2, the weight table was renamed to progress,
                    //and the column weight was renamed to measurement.  Also, an additional column
                    //measurement_type was added to the exercise table.
                    db.execSQL("ALTER TABLE exercise RENAME to exercise_orig");
                    db.execSQL("CREATE TABLE exercise AS SELECT _id, day_id, repetitions, sets, description, weight AS measurement FROM exercise_orig");
                    //all prior measurements were weights, so the default works perfectly
                    db.execSQL("ALTER TABLE exercise ADD COLUMN measurement_type INTEGER DEFAULT " + Exercise.MEASUREMENT_TYPE_WEIGHT);
                    db.execSQL("CREATE TABLE progress AS SELECT _id, exercise_id, date, weight AS measurement FROM weight");
                    db.execSQL("DROP TABLE IF EXISTS exercise_orig");
                    db.execSQL("DROP TABLE IF EXISTS weight");
                    break;
                case 3:
                    //From version 2 to 3, a linker table was added to enforce an N:N relationship between exercises and days.
                    db.execSQL(SQL_CREATE_EXERCISE_DAY_LINKER_TABLE);
                    //copy the old foreign key associations
                    db.execSQL("INSERT INTO exercise_day_linker (day_key, exercise_key) SELECT day_id, exercise._id FROM exercise");
                    //to remove the foreign key from the exercise table, the table must be deleted and recreated.
                    db.execSQL("ALTER TABLE exercise RENAME to exercise_orig");
                    db.execSQL("CREATE TABLE exercise AS SELECT _id, repetitions, sets, description, measurement FROM exercise_orig");
                    db.execSQL("DROP TABLE IF EXISTS exercise_orig");
                    break;
            }
            upgradeTo++;
        }
    }


}
