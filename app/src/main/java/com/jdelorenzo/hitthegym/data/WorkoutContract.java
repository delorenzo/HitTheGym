package com.jdelorenzo.hitthegym.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class WorkoutContract {
    public static final String CONTENT_AUTHORITY = "com.jdelorenzo.hitthegym.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ROUTINE = "routine";
    public static final String PATH_DAY = "dayId";
    public static final String PATH_DAY_OF_WEEK = "dayOfWeek";
    public static final String PATH_EXERCISE = "exercise";
    public static final String PATH_NAME = "name";
    public static final String PATH_PROGRESS = "progress";
    public static final String PATH_LINKER = "day_exercise_linker";

    public static final class RoutineEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTINE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ROUTINE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +  CONTENT_AUTHORITY + "/" + PATH_ROUTINE;

        public static final String TABLE_NAME = "routine";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LAST_MODIFIED = "last_modified";

        public static Uri buildRoutineId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getRoutineIdFromUri(Uri uri) {
            String workoutIdString = uri.getPathSegments().get(1);
            if (null != workoutIdString && workoutIdString.length() > 0)
                return Long.parseLong(workoutIdString);
            else
                return 0;
        }

        public static Uri buildRoutineName(String name) {
            return CONTENT_URI.buildUpon().appendQueryParameter(PATH_NAME, name).build();
        }

        public static String getRoutineNameFromUri(Uri uri) {
            String name = uri.getPathSegments().get(2);
            return uri.getPathSegments().get(2);
        }
    }

    public static final class DayEntry implements  BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DAY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DAY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DAY;

        public static final String TABLE_NAME = "day";

        public static final String COLUMN_ROUTINE_KEY = "workout_id";
        public static final String COLUMN_DAY_OF_WEEK = "day_of_week";
        public static final String COLUMN_LAST_DATE = "last_date";

        public static Uri buildDayId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildRoutineId(long id) {
            return CONTENT_URI.buildUpon().appendPath(PATH_ROUTINE).appendPath(Long.toString(id))
                    .build();
        }

        public static Uri buildRoutineIdDayOfWeek(long id, long day) {
            return CONTENT_URI.buildUpon().appendPath(PATH_ROUTINE).appendPath(Long.toString(id))
                    .appendPath(PATH_DAY).appendPath(Long.toString(day)).build();
        }

        public static long getDayIdFromUri(Uri uri) {
            String dayIdString = uri.getPathSegments().get(1);
            if (null != dayIdString && dayIdString.length() > 0)
                return Long.parseLong(dayIdString);
            else
                return 0;
        }

        public static long getRoutineIdFromUri(Uri uri) {
            String routineIdString = uri.getPathSegments().get(2);
            if (null != routineIdString && routineIdString.length() > 0)
                return Long.parseLong(routineIdString);
            else
                return 0;
        }

        public static String getDayOfWeekFromUri(Uri uri) {
            return uri.getPathSegments().get(4);
        }
    }

    public static final class ExerciseEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EXERCISE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_EXERCISE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_EXERCISE;

        public static final String TABLE_NAME = "exercise";

        public static final String COLUMN_REPS = "repetitions";
        public static final String COLUMN_SETS = "sets";
        public static final String COLUMN_MEASUREMENT = "measurement";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_MEASUREMENT_TYPE = "measurement_type";

        public static Uri buildExerciseId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDayId(long dayId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_DAY).appendPath(Long.toString(dayId))
                    .build();
        }

        public static Uri buildDayOfWeek(int day) {
            return CONTENT_URI.buildUpon().appendPath(PATH_DAY_OF_WEEK).appendPath(Integer.toString(day))
                    .build();
        }

        public static Uri buildRoutineIdDayOfWeek(long routineId, int day) {
            return CONTENT_URI.buildUpon().appendPath(PATH_DAY_OF_WEEK).appendPath(Integer.toString(day))
                    .appendPath(PATH_ROUTINE).appendPath(Long.toString(routineId)).build();
        }

        public static long getExerciseIdFromUri(Uri uri) {
            String exerciseIdString = uri.getPathSegments().get(1);
            if (null != exerciseIdString && exerciseIdString.length() > 0)
                return Long.parseLong(exerciseIdString);
            else
                return 0;
        }

        public static long getDayIdFromUri(Uri uri) {
            String dayIdString = uri.getPathSegments().get(2);
            if (null != dayIdString && dayIdString.length() > 0)
                return Long.parseLong(dayIdString);
            else
                return 0;
        }

        public static long getRoutineIdFromUri(Uri uri) {
            String routineIdString = uri.getPathSegments().get(4);
            if (null != routineIdString && routineIdString.length() > 0)
                return Long.parseLong(routineIdString);
            else
                return 0;
        }

        public static int getDayOfWeekFromUri(Uri uri) {
            String dayOfWeekString = uri.getPathSegments().get(2);
            if (null != dayOfWeekString && dayOfWeekString.length() > 0)
                return Integer.parseInt(dayOfWeekString);
            else
                return 0;
        }
    }

    public static final class ProgressEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROGRESS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_PROGRESS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +
                        PATH_PROGRESS;

        public static final String TABLE_NAME = "progress";

        public static final String COLUMN_MEASUREMENT = "measurement";
        public static final String COLUMN_EXERCISE_KEY = "exercise_id";
        public static final String COLUMN_DATE = "date";

        public static Uri buildProgressId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getProgressIdFromUri(Uri uri) {
            String weightIdString = uri.getPathSegments().get(1);
            if (null != weightIdString && weightIdString.length() > 0)
                return Long.parseLong(weightIdString);
            else
                return 0;
        }

        public static Uri buildExerciseId(long id) {
            return CONTENT_URI.buildUpon().appendPath(PATH_EXERCISE).appendPath(Long.toString(id))
                    .build();
        }

        public static long getExerciseIdFromUri(Uri uri) {
            String weightIdString = uri.getPathSegments().get(2);
            if (null != weightIdString && weightIdString.length() > 0)
                return Long.parseLong(weightIdString);
            else
                return 0;
        }
    }

    public static final class ExerciseDayLinkerEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LINKER).build();

        public static final String TABLE_NAME = "exercise_day_linker";
        public static final String COLUMN_DAY_KEY = "day_key";
        public static final String COLUMN_EXERCISE_KEY = "exercise_key";

        public static Uri buildId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDayIdExerciseId(long dayId, long exerciseId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_DAY).appendPath(Long.toString(dayId))
                    .appendPath(PATH_EXERCISE).appendPath(Long.toString(exerciseId)).build();
        }

        public static long getIdFromUri(Uri uri) {
            String idString = uri.getPathSegments().get(1);
            if (null != idString && idString.length() > 0)
                return Long.parseLong(idString);
            else
                return 0;
        }

        public static long getExerciseIdFromUri(Uri uri) {
            String exerciseIdString = uri.getPathSegments().get(4);
            if (null != exerciseIdString && exerciseIdString.length() > 0)
                return Long.parseLong(exerciseIdString);
            else
                return 0;
        }

        public static long getDayIdFromUri(Uri uri) {
            String dayIdString = uri.getPathSegments().get(2);
            if (null != dayIdString && dayIdString.length() > 0)
                return Long.parseLong(dayIdString);
            else
                return 0;
        }
    }
}
