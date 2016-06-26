package com.jdelorenzo.hitthegym.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.jdelorenzo.hitthegym.R;
import com.jdelorenzo.hitthegym.Utility;
import com.jdelorenzo.hitthegym.data.WorkoutContract;
import com.jdelorenzo.hitthegym.model.Exercise;

import java.util.Calendar;
import java.util.Locale;

/*
WidgetService is the {@link RemoteViewsService} that will return the widget's RemoteViewsFactory
 */
public class WidgetService extends RemoteViewsService {
    private static final String LOG_TAG = WidgetService.class.getSimpleName();

    public static final String[] EXERCISE_COLUMNS = {
            WorkoutContract.ExerciseEntry.TABLE_NAME + "." + WorkoutContract.ExerciseEntry._ID,
            WorkoutContract.ExerciseEntry.COLUMN_MEASUREMENT,
            WorkoutContract.ExerciseEntry.COLUMN_SETS,
            WorkoutContract.ExerciseEntry.COLUMN_REPS,
            WorkoutContract.ExerciseEntry.COLUMN_DESCRIPTION,
            WorkoutContract.ExerciseEntry.COLUMN_MEASUREMENT_TYPE
    };
    public final static int COL_EXERCISE_ID = 0;
    public final static int COL_MEASUREMENT = 1;
    public final static int COL_SETS = 2;
    public final static int COL_REPS = 3;
    public final static int COL_DESCRIPTION = 4;
    public final static int COL_MEASUREMENT_TYPE = 5;

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                Log.v(LOG_TAG, "Widget data set changed");
                if (data != null) {
                    data.close();
                }
                /*
                Because the content provider is not exported, the calling identity must be
                cleared and then restored so that calls use the correct process and permission
                 */
                final long identityToken = Binder.clearCallingIdentity();
                Calendar calendar = Calendar.getInstance();
                //the days are indexed by 0, calendar is indexed by 1
                int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                Uri exerciseUri = WorkoutContract.ExerciseEntry.buildDayOfWeek(day);
                data = getContentResolver().query(
                        exerciseUri,
                        EXERCISE_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (data == null || position == AdapterView.INVALID_POSITION
                        || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);
                String name = data.getString(COL_DESCRIPTION);
                int reps = data.getInt(COL_REPS);
                int sets = data.getInt(COL_SETS);
                double measurement = data.getDouble(COL_MEASUREMENT);
                @Exercise.MeasurementType int measurementType = data.getInt(COL_MEASUREMENT_TYPE);
                String setsRepsString = String.format(Locale.getDefault(),
                        getString(R.string.reps_sets_format), reps, sets);
                String weightString = Utility.getFormattedMeasurementString(getApplicationContext(),
                        measurement, measurementType);
                views.setTextViewText(R.id.widget_exercise_name, name);
                views.setTextViewText(R.id.widget_reps_sets, setsRepsString);
                views.setTextViewText(R.id.widget_weight, weightString);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data != null && data.moveToPosition(position)) {
                    return data.getLong(COL_EXERCISE_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
