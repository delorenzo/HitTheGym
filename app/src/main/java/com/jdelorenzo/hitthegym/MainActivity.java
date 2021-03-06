package com.jdelorenzo.hitthegym;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.jdelorenzo.hitthegym.data.WorkoutContract;
import com.jdelorenzo.hitthegym.dialogs.CreateRoutineDialogFragment;
import com.jdelorenzo.hitthegym.dialogs.SelectRoutineDialogFragment;

import java.util.List;
import java.util.Locale;

import butterknife.Action;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.ViewCollections;

public class MainActivity extends AppCompatActivity implements
        CreateRoutineDialogFragment.CreateRoutineDialogListener{

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindViews({R.id.button_work_out, R.id.button_edit_workout, R.id.button_view_stats}) List<Button> workoutButtons;
    @BindView(R.id.button_view_stats) Button viewStatsButton;
    private static final String FTAG_DIALOG_FRAGMENT = "CreateRoutineDialogFragment";
    String[] mRoutineLabels = new String[]{};
    long[] mRoutineIds = new long[]{};
    public static final String ARG_NAME = "name";
    private String displayName;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Bundle args = getIntent().getExtras();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        displayName = sharedPreferences.getString(getString(R.string.prefs_display_name_key), "");
        if (!displayName.isEmpty()) {
            viewStatsButton.setText(String.format(Locale.getDefault(),
                    getString(R.string.format_button_text), displayName));
        }

        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        new RetrieveWorkoutsTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_NAME, displayName);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        displayName = savedInstanceState.getString(ARG_NAME);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.button_work_out)
    public void onWorkOut() {
        if (mRoutineIds == null || mRoutineIds.length == 0) {
            Toast.makeText(this, getString(R.string.empty_routines_text), Toast.LENGTH_SHORT).show();
        }
        else if (mRoutineIds.length > 1) {
            DialogFragment selectWorkoutFragment = SelectRoutineDialogFragment.newInstance(new SelectRoutineDialogFragment.SelectRoutineListener() {
                @Override
                public void onRoutineSelected(long id) {
                    workOut(id);
                }
            }, mRoutineLabels, mRoutineIds);
            selectWorkoutFragment.show(getFragmentManager(), FTAG_DIALOG_FRAGMENT);
        }
        else {
            workOut(mRoutineIds[0]);
        }
    }

    private void workOut(long workoutId) {
        Intent intent = new Intent(this, WorkoutActivity.class);
        intent.putExtra(WorkoutActivity.ARG_ROUTINE_ID, workoutId);
        startActivity(intent);
    }

    @OnClick(R.id.button_create_workout)
    public void onCreateWorkout() {
        DialogFragment dialog = new CreateRoutineDialogFragment();
        dialog.show(getFragmentManager(), FTAG_DIALOG_FRAGMENT);
    }

    @OnClick(R.id.button_edit_workout)
    public void onEditWorkout() {
        if (mRoutineIds == null) return;
        if (mRoutineIds.length == 0) {
            Toast.makeText(MainActivity.this, getString(R.string.empty_routines_text), Toast.LENGTH_SHORT).show();
        }
        else if (mRoutineIds.length == 1) {
            modifyWorkout(mRoutineIds[0]);
        }
        else {
            DialogFragment selectWorkoutFragment = SelectRoutineDialogFragment.newInstance(new SelectRoutineDialogFragment.SelectRoutineListener() {
                @Override
                public void onRoutineSelected(long id) {
                    modifyWorkout(id);
                }
            }, mRoutineLabels, mRoutineIds);
            selectWorkoutFragment.show(getFragmentManager(), FTAG_DIALOG_FRAGMENT);
        }

    }

    private void modifyWorkout(long workout) {
        Intent intent = new Intent(this, ModifyRoutineActivity.class);
        intent.putExtra(ModifyRoutineActivity.ARG_WORKOUT_ID, workout);
        startActivity(intent);
    }

    @OnClick(R.id.button_view_stats)
    public void onViewStats() {
        Intent intent = new Intent(this, ViewStatsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRoutineCreated(String name) {
        //for just this case we're doing the database insert with an async task because
        //we need the routine ID to start the next activity, which isn't known until the
        //insert completes.
        new CreateWorkoutTask().execute(name);
    }



    static final Action<View> DISABLE = new Action<View>() {
        @Override
        public void apply(@NonNull View view, int index) {
            view.setAlpha(0.2f);
            view.setClickable(false);
        }
    };

    private class RetrieveWorkoutsTask extends AsyncTask<Void, Void, long[]> {
        public String WORKOUT_COLUMNS[] = {
                WorkoutContract.RoutineEntry.TABLE_NAME + "." + WorkoutContract.RoutineEntry._ID,
                WorkoutContract.RoutineEntry.COLUMN_NAME
        };
        public int COL_ID = 0;
        public int COL_NAME = 1;

        @Override
        protected long [] doInBackground(Void... params) {
            Cursor retCursor = getContentResolver().query(
                    WorkoutContract.RoutineEntry.CONTENT_URI,
                    WORKOUT_COLUMNS,
                    null,
                    null,
                    null
            );
            if (retCursor != null && retCursor.moveToFirst()) {
                int count = retCursor.getCount();
                mRoutineIds = new long[count];
                mRoutineLabels = new String[count];
                for (int i = 0; i < count; i++) {
                    mRoutineIds[i] = retCursor.getLong(COL_ID);
                    mRoutineLabels[i] = retCursor.getString(COL_NAME);
                    retCursor.moveToNext();
                }
                retCursor.close();
            }
            return mRoutineIds;
        }

        @Override
        protected void onPostExecute(long[] result) {
            if (null == result || result.length == 0) {
                ViewCollections.run(workoutButtons, DISABLE);
            }
        }
    }

    private class CreateWorkoutTask extends AsyncTask<String, Void, Long> {
        @Override
        protected Long doInBackground(String... params) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(WorkoutContract.RoutineEntry.COLUMN_NAME,
                    params[0]);
            Uri retUri = getApplicationContext().getContentResolver().insert(
                    WorkoutContract.RoutineEntry.CONTENT_URI,
                    contentValues);
            return WorkoutContract.RoutineEntry.getRoutineIdFromUri(retUri);
        }

        @Override
        protected void onPostExecute(Long id) {
            modifyWorkout(id);
        }
    }
}
