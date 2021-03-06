package com.jdelorenzo.hitthegym;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.jdelorenzo.hitthegym.adapters.EditExerciseAdapter;
import com.jdelorenzo.hitthegym.data.WorkoutContract;
import com.jdelorenzo.hitthegym.dialogs.EditExerciseDialogFragment;
import com.jdelorenzo.hitthegym.model.Exercise;
import com.jdelorenzo.hitthegym.service.DatabaseIntentService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditWorkoutFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_ROUTINE_ID = "routineId";
    private static final String ARG_DAY_ID = "dayId";
    private static final String ARG_CALLBACK = "callback";
    private static final String FTAG_EDIT_EXERCISE = "editExerciseFragment";
    private long mWorkoutId;
    private long mDayId;
    private EditExerciseAdapter mAdapter;
    @BindView(R.id.empty_exercise_text_view) TextView mEmptyView;
    @BindView(R.id.exercise_recyclerview) RecyclerView mRecyclerView;
    private static final int WORKOUT_LOADER = 0;
    private Unbinder unbinder;

    public String[] EXERCISE_COLUMNS = {
            WorkoutContract.ExerciseEntry.TABLE_NAME + "." + WorkoutContract.ExerciseEntry._ID,
            WorkoutContract.ExerciseEntry.COLUMN_MEASUREMENT,
            WorkoutContract.ExerciseEntry.COLUMN_SETS,
            WorkoutContract.ExerciseEntry.COLUMN_REPS,
            WorkoutContract.ExerciseEntry.COLUMN_DESCRIPTION,
            WorkoutContract.ExerciseEntry.COLUMN_MEASUREMENT_TYPE
    };
    public final static int COL_EXERCISE_ID = 0;
    public final static int COL_WEIGHT = 1;
    public final static int COL_SETS = 2;
    public final static int COL_REPS = 3;
    public final static int COL_DESCRIPTION = 4;
    public final static int COL_MEASUREMENT_TYPE = 5;

    public static EditWorkoutFragment newInstance(long workoutId, long dayId) {
        EditWorkoutFragment fragment = new EditWorkoutFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_ROUTINE_ID, workoutId);
        b.putLong(ARG_DAY_ID, dayId);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWorkoutId = getArguments().getLong(ARG_ROUTINE_ID);
            mDayId = getArguments().getLong(ARG_DAY_ID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mWorkoutId = getArguments().getLong(ARG_ROUTINE_ID);
            mDayId = getArguments().getLong(ARG_DAY_ID);
        }
        getLoaderManager().initLoader(WORKOUT_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(ARG_ROUTINE_ID, mWorkoutId);
        outState.putLong(ARG_DAY_ID, mDayId);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_workout_day, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new EditExerciseAdapter(getActivity(), new EditExerciseAdapter.ExerciseAdapterOnClickHandler() {
            @Override
            public void onClick(final Long id, Exercise exercise,
                                EditExerciseAdapter.ExerciseAdapterViewHolder vh) {
                EditExerciseDialogFragment fragment = EditExerciseDialogFragment.newInstance(
                        exercise,
                        new EditExerciseDialogFragment.EditExerciseDialogFragmentListener() {
                            @Override
                            public void onEditExercise(Exercise exercise) {
                                DatabaseIntentService.startActionEditExercise(getActivity(),
                                        id, exercise);
                                getActivity().getContentResolver().notifyChange(
                                        WorkoutContract.ExerciseEntry.buildDayId(mDayId), null);
                            }
                        });
                fragment.show(getFragmentManager(), FTAG_EDIT_EXERCISE);
            }

            @Override
            public void onDelete(Long id, EditExerciseAdapter.ExerciseAdapterViewHolder vh) {
                DatabaseIntentService.startActionDeleteExerciseFromDay(getActivity(), id, mDayId);
                getActivity().getContentResolver().notifyChange(WorkoutContract.ExerciseEntry.buildDayId(mDayId), null);
            }
        }, mEmptyView, ListView.CHOICE_MODE_SINGLE);
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri dayForWorkoutUri = WorkoutContract.ExerciseEntry.buildDayId(mDayId);
        return new CursorLoader(getActivity(),
                dayForWorkoutUri,
                EXERCISE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public long getDayId() {
        return mDayId;
    }
}
