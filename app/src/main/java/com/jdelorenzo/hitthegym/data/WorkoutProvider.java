package com.jdelorenzo.hitthegym.data;

import android.app.backup.BackupManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.jdelorenzo.hitthegym.data.WorkoutContract.*;

public class WorkoutProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WorkoutDbHelper mOpenHelper;
    private static final String LOG_TAG = WorkoutProvider.class.getSimpleName();

    private static final String sRoutineIdSelection = RoutineEntry.TABLE_NAME + "." +
            RoutineEntry._ID + " = ?";
    private static final String sDayIdSelection = DayEntry.TABLE_NAME + "." +
            DayEntry._ID + " = ?";
    private static  final String sExerciseIdSelection = ExerciseEntry.TABLE_NAME + "." +
            ExerciseEntry._ID + " = ?";
    private static final String sRoutineIdDayOfWeekSelection = RoutineEntry.TABLE_NAME + "." +
            RoutineEntry._ID + " = ? AND " + DayEntry.TABLE_NAME + "." +
            DayEntry.COLUMN_DAY_OF_WEEK + " = ?";
    private static final String sDayRoutineKeySelection = DayEntry.TABLE_NAME + "." +
            DayEntry.COLUMN_ROUTINE_KEY + " = ?";
    private static final String sExerciseDayIdSelection = ExerciseDayLinkerEntry.TABLE_NAME + "." +
            ExerciseDayLinkerEntry.COLUMN_DAY_KEY + " = ?";
    private static final String sExerciseDayOfWeekSelection = DayEntry.COLUMN_DAY_OF_WEEK + " = ?";
    private static final String sExerciseRoutineIdDayOfWeekSelection = DayEntry.TABLE_NAME + "." +
            DayEntry.COLUMN_ROUTINE_KEY + " = ? AND " + DayEntry.TABLE_NAME + "." +
            DayEntry.COLUMN_DAY_OF_WEEK + " = ?";
    private static final String sWeightExerciseIdSelection = ProgressEntry.TABLE_NAME + "." +
            ProgressEntry.COLUMN_EXERCISE_KEY + " = ?";
    private static final String sLinkByDayAndExerciseSelection = ExerciseDayLinkerEntry.TABLE_NAME
            + "." + ExerciseDayLinkerEntry.COLUMN_DAY_KEY + " = ? AND " +
            ExerciseDayLinkerEntry.TABLE_NAME + "." + ExerciseDayLinkerEntry.COLUMN_EXERCISE_KEY +
            " = ?";

    static final int ROUTINES = 100;
    static final int ROUTINE_WITH_ID = 101;
    static final int DAYS = 200;
    static final int DAY_WITH_ID = 201;
    static final int DAYS_WITH_ROUTINE_ID = 202;
    static final int DAY_WITH_ROUTINE_ID_AND_DAY_OF_WEEK = 203;
    static final int EXERCISES = 300;
    static final int EXERCISE_WITH_ID = 301;
    static final int EXERCISES_WITH_ROUTINE_ID = 302;
    static final int EXERCISES_WITH_DAY_ID = 303;
    static final int EXERCISES_WITH_DAY_OF_WEEK = 304;
    static final int EXERCISES_WITH_ROUTINE_ID_AND_DAY_OF_WEEK = 305;
    static final int PROGRESS = 400;
    static final int PROGRESS_WITH_EXERCISE_ID = 401;
    static final int DAY_EXERCISE_LINK = 500;
    static final int DAY_EXERCISE_LINK_WITH_DAY_AND_EXERCISE = 501;

    private static final SQLiteQueryBuilder sDayByRoutineQueryBuilder;

    static {
        sDayByRoutineQueryBuilder = new SQLiteQueryBuilder();

        sDayByRoutineQueryBuilder.setTables(
                RoutineEntry.TABLE_NAME + " INNER JOIN " + DayEntry.TABLE_NAME +
                        " ON " + RoutineEntry.TABLE_NAME + "." + RoutineEntry._ID + " = " +
                        DayEntry.TABLE_NAME + "." + DayEntry.COLUMN_ROUTINE_KEY
        );
    }

    private static final SQLiteQueryBuilder sExerciseByDayQueryBuilder;

    static {
        sExerciseByDayQueryBuilder = new SQLiteQueryBuilder();

        sExerciseByDayQueryBuilder.setTables(
                DayEntry.TABLE_NAME + " INNER JOIN " + ExerciseDayLinkerEntry.TABLE_NAME +
                        " ON " + DayEntry.TABLE_NAME + "." + DayEntry._ID +
                        " = " + ExerciseDayLinkerEntry.TABLE_NAME + "." +
                        ExerciseDayLinkerEntry.COLUMN_DAY_KEY
                        + " INNER JOIN " + ExerciseEntry.TABLE_NAME + " ON " +
                        ExerciseEntry.TABLE_NAME + "." + ExerciseEntry._ID +
                        " = " + ExerciseDayLinkerEntry.TABLE_NAME + "." +
                        ExerciseDayLinkerEntry.COLUMN_EXERCISE_KEY
        );
    }

    private static final SQLiteQueryBuilder sProgressQueryBuilder;

    static {
        sProgressQueryBuilder = new SQLiteQueryBuilder();

        sProgressQueryBuilder.setTables(
                ProgressEntry.TABLE_NAME + " INNER JOIN " + ExerciseEntry.TABLE_NAME +
                        " ON " + ProgressEntry.TABLE_NAME + "." + ProgressEntry.COLUMN_EXERCISE_KEY
                + " = " + ExerciseEntry.TABLE_NAME + "." + ExerciseEntry._ID
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new WorkoutDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case ROUTINES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RoutineEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            case ROUTINE_WITH_ID:
                long workoutId = RoutineEntry.getRoutineIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RoutineEntry.TABLE_NAME,
                        projection,
                        sRoutineIdSelection,
                        new String [] {Long.toString(workoutId)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case DAYS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DayEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            case DAY_WITH_ID:
                long dayId = DayEntry.getDayIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DayEntry.TABLE_NAME,
                        projection,
                        sDayIdSelection,
                        new String[] {Long.toString(dayId)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case DAYS_WITH_ROUTINE_ID:
                workoutId = DayEntry.getRoutineIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DayEntry.TABLE_NAME,
                        projection,
                        sDayRoutineKeySelection,
                        new String [] {Long.toString(workoutId)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case DAY_WITH_ROUTINE_ID_AND_DAY_OF_WEEK:
                workoutId = DayEntry.getRoutineIdFromUri(uri);
                String dayOfWeek = DayEntry.getDayOfWeekFromUri(uri);
                retCursor = sDayByRoutineQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sRoutineIdDayOfWeekSelection,
                        new String [] {Long.toString(workoutId), dayOfWeek},
                        null,
                        null,
                        sortOrder
                );
                break;
            case EXERCISES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ExerciseEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case EXERCISE_WITH_ID:
                long exerciseId = ExerciseEntry.getExerciseIdFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ExerciseEntry.TABLE_NAME,
                        projection,
                        sExerciseIdSelection,
                        new String [] {Long.toString(exerciseId)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case EXERCISES_WITH_DAY_ID:
                dayId = ExerciseEntry.getDayIdFromUri(uri);
                retCursor = sExerciseByDayQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sExerciseDayIdSelection,
                        new String[] {Long.toString(dayId)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case EXERCISES_WITH_DAY_OF_WEEK:
                int day = ExerciseEntry.getDayOfWeekFromUri(uri);
                retCursor = sExerciseByDayQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sExerciseDayOfWeekSelection,
                        new String[] {Integer.toString(day)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case EXERCISES_WITH_ROUTINE_ID_AND_DAY_OF_WEEK:
                workoutId = ExerciseEntry.getRoutineIdFromUri(uri);
                day = ExerciseEntry.getDayOfWeekFromUri(uri);
                retCursor = sExerciseByDayQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sExerciseRoutineIdDayOfWeekSelection,
                        new String[] {Long.toString(workoutId), Integer.toString(day)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case PROGRESS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ProgressEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PROGRESS_WITH_EXERCISE_ID:
                exerciseId = ProgressEntry.getExerciseIdFromUri(uri);
                retCursor = sProgressQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sWeightExerciseIdSelection,
                        new String[] { Long.toString(exerciseId)},
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:  " + uri);
        }
                     if (getContext() != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
         return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ROUTINES:
                return RoutineEntry.CONTENT_TYPE;
            case ROUTINE_WITH_ID:
                return RoutineEntry.CONTENT_ITEM_TYPE;
            case DAYS:
                return DayEntry.CONTENT_TYPE;
            case DAY_WITH_ID:
                return DayEntry.CONTENT_ITEM_TYPE;
            case DAY_WITH_ROUTINE_ID_AND_DAY_OF_WEEK:
                return DayEntry.CONTENT_ITEM_TYPE;
            case DAYS_WITH_ROUTINE_ID:
                return DayEntry.CONTENT_TYPE;
            case EXERCISES:
                return ExerciseEntry.CONTENT_TYPE;
            case EXERCISE_WITH_ID:
                return ExerciseEntry.CONTENT_ITEM_TYPE;
            case EXERCISES_WITH_DAY_ID:
                return ExerciseEntry.CONTENT_TYPE;
            case EXERCISES_WITH_ROUTINE_ID:
                return ExerciseEntry.CONTENT_TYPE;
            case EXERCISES_WITH_DAY_OF_WEEK:
                return ExerciseEntry.CONTENT_TYPE;
            case EXERCISES_WITH_ROUTINE_ID_AND_DAY_OF_WEEK:
                return ExerciseEntry.CONTENT_TYPE;
            case PROGRESS:
                return ProgressEntry.CONTENT_TYPE;
            case PROGRESS_WITH_EXERCISE_ID:
                return ProgressEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri:  " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ROUTINES:
                long routineId = db.insert(RoutineEntry.TABLE_NAME, null, values);
                if (routineId > 0)
                    returnUri = RoutineEntry.buildRoutineId(routineId);
                else
                    throw new android.database.SQLException("Failed to insert routine row into " + uri);
                break;
            case DAYS:
                long dayId = db.insert(DayEntry.TABLE_NAME, null, values);
                if (dayId > 0)
                    returnUri = DayEntry.buildDayId(dayId);
                else
                    throw new android.database.SQLException("Failed to insert day row into " + uri);
                break;
            case EXERCISES:
                long exerciseId = db.insert(ExerciseEntry.TABLE_NAME, null, values);
                if (exerciseId > 0)
                    returnUri = ExerciseEntry.buildExerciseId(exerciseId);
                else
                    throw new android.database.SQLException("Failed to insert exercise row into " + uri);
                break;
            case EXERCISES_WITH_DAY_ID:
                exerciseId = db.insert(ExerciseEntry.TABLE_NAME, null, values);
                if (exerciseId > 0)
                    returnUri = ExerciseEntry.buildExerciseId(exerciseId);
                else
                    throw new android.database.SQLException("Failed to insert exercise row into " + uri);
                break;
            case PROGRESS:
                long weightId = db.insert(ProgressEntry.TABLE_NAME, null, values);
                if (weightId > 0) {
                    returnUri = ProgressEntry.buildProgressId(weightId);
                }
                else
                    throw new android.database.SQLException("Failed to insert progress row into " + uri);
                break;
            case DAY_EXERCISE_LINK:
                long id = db.insert(ExerciseDayLinkerEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ExerciseDayLinkerEntry.buildId(id);
                }
                else
                    throw new android.database.SQLException("Failed to insert link row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri in inset:  " + uri);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case ROUTINES:
                rowsDeleted = db.delete(RoutineEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case ROUTINE_WITH_ID:
                long workoutId = RoutineEntry.getRoutineIdFromUri(uri);
                rowsDeleted = db.delete(RoutineEntry.TABLE_NAME,
                        sRoutineIdSelection,
                        new String[] {Long.toString(workoutId)});
                break;
            case DAYS:
                rowsDeleted = db.delete(DayEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case DAY_WITH_ID:
                long dayId = DayEntry.getDayIdFromUri(uri);
                rowsDeleted = db.delete(DayEntry.TABLE_NAME,
                        sDayIdSelection,
                        new String[] {Long.toString(dayId)});
                break;
            case DAYS_WITH_ROUTINE_ID:
                workoutId = DayEntry.getRoutineIdFromUri(uri);
                rowsDeleted = db.delete(DayEntry.TABLE_NAME,
                        sDayRoutineKeySelection,
                        new String[] {Long.toString(workoutId)});
                break;
            case EXERCISES:
                rowsDeleted = db.delete(ExerciseEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case EXERCISE_WITH_ID:
                long exerciseId = ExerciseEntry.getExerciseIdFromUri(uri);
                rowsDeleted = db.delete(ExerciseEntry.TABLE_NAME,
                        sExerciseIdSelection,
                        new String[] {Long.toString(exerciseId)});
                break;
            case DAY_EXERCISE_LINK_WITH_DAY_AND_EXERCISE:
                dayId = ExerciseDayLinkerEntry.getDayIdFromUri(uri);
                exerciseId = ExerciseDayLinkerEntry.getExerciseIdFromUri(uri);
                rowsDeleted = db.delete(ExerciseDayLinkerEntry.TABLE_NAME,
                        sLinkByDayAndExerciseSelection,
                        new String[] {Long.toString(dayId), Long.toString(exerciseId)});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri in delete:  " + uri);
        }
        if (rowsDeleted != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        requestBackup();
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ROUTINE_WITH_ID:
                long routineId = RoutineEntry.getRoutineIdFromUri(uri);
                rowsUpdated = db.update(RoutineEntry.TABLE_NAME,
                        values,
                        sRoutineIdSelection,
                        new String[] {Long.toString(routineId)});
                break;
            case DAY_WITH_ID:
                long dayId = DayEntry.getDayIdFromUri(uri);
                rowsUpdated = db.update(DayEntry.TABLE_NAME,
                        values,
                        sDayIdSelection,
                        new String[] {Long.toString(dayId)});
                break;
            case EXERCISE_WITH_ID:
                long exerciseId = ExerciseEntry.getExerciseIdFromUri(uri);
                rowsUpdated = db.update(ExerciseEntry.TABLE_NAME,
                        values,
                        sExerciseIdSelection,
                        new String[] {Long.toString(exerciseId)});
                break;
            case EXERCISES_WITH_DAY_ID:
                dayId = ExerciseEntry.getDayIdFromUri(uri);
                rowsUpdated = db.update(ExerciseEntry.TABLE_NAME,
                        values,
                        sExerciseDayIdSelection,
                        new String[] {Long.toString(dayId)});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri in update:  " + uri);
        }
        if (rowsUpdated != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        requestBackup();
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;
        switch (match) {
            case ROUTINES:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RoutineEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case DAYS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DayEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case DAYS_WITH_ROUTINE_ID:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DayEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case EXERCISES:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ExerciseEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case EXERCISES_WITH_DAY_ID:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ExerciseEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case PROGRESS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ProgressEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri in bulk insert:  " + uri.toString());
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        Log.d(LOG_TAG, "Inserted " + returnCount + " into database");
        requestBackup();
        return returnCount;
    }

    /*
    Notify the backup manager that data has changed.
     */
    private void requestBackup() {
        BackupManager bm = new BackupManager(getContext());
        bm.dataChanged();
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WorkoutContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, WorkoutContract.PATH_ROUTINE, ROUTINES);
        matcher.addURI(authority, WorkoutContract.PATH_ROUTINE + "/#", ROUTINE_WITH_ID);

        matcher.addURI(authority, WorkoutContract.PATH_DAY, DAYS);
        matcher.addURI(authority, WorkoutContract.PATH_DAY + "/#", DAY_WITH_ID);
        matcher.addURI(authority, WorkoutContract.PATH_DAY + "/" + WorkoutContract.PATH_ROUTINE +
        "/#", DAYS_WITH_ROUTINE_ID);
        matcher.addURI(authority, WorkoutContract.PATH_DAY + "/" + WorkoutContract.PATH_ROUTINE +
                "/#/" + WorkoutContract.PATH_DAY + "/*", DAY_WITH_ROUTINE_ID_AND_DAY_OF_WEEK);

        matcher.addURI(authority, WorkoutContract.PATH_EXERCISE, EXERCISES);
        matcher.addURI(authority, WorkoutContract.PATH_EXERCISE + "/#", EXERCISE_WITH_ID);
        matcher.addURI(authority, WorkoutContract.PATH_EXERCISE + "/" +
                WorkoutContract.PATH_ROUTINE + "/#", EXERCISES_WITH_ROUTINE_ID);
        matcher.addURI(authority, WorkoutContract.PATH_EXERCISE + "/" + WorkoutContract.PATH_DAY +
                "/#", EXERCISES_WITH_DAY_ID);
        matcher.addURI(authority, WorkoutContract.PATH_EXERCISE + "/" +
                WorkoutContract.PATH_DAY_OF_WEEK + "/#/" + WorkoutContract.PATH_ROUTINE + "/#",
                EXERCISES_WITH_ROUTINE_ID_AND_DAY_OF_WEEK);
        matcher.addURI(authority, WorkoutContract.PATH_EXERCISE + "/" +
                WorkoutContract.PATH_DAY_OF_WEEK + "/#", EXERCISES_WITH_DAY_OF_WEEK);

        matcher.addURI(authority, WorkoutContract.PATH_PROGRESS, PROGRESS);
        matcher.addURI(authority, WorkoutContract.PATH_PROGRESS + "/" +
                WorkoutContract.PATH_EXERCISE + "/#", PROGRESS_WITH_EXERCISE_ID);

        matcher.addURI(authority, WorkoutContract.PATH_LINKER, DAY_EXERCISE_LINK);
        matcher.addURI(authority, WorkoutContract.PATH_LINKER + "/" + WorkoutContract.PATH_DAY +
                "/#/" + WorkoutContract.PATH_EXERCISE + "/#",
                DAY_EXERCISE_LINK_WITH_DAY_AND_EXERCISE);

        return matcher;
    }
}
