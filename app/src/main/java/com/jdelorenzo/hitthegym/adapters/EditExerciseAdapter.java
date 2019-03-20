package com.jdelorenzo.hitthegym.adapters;

import android.content.Context;
import android.database.Cursor;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jdelorenzo.hitthegym.EditWorkoutFragment;
import com.jdelorenzo.hitthegym.R;
import com.jdelorenzo.hitthegym.Utility;
import com.jdelorenzo.hitthegym.data.WorkoutContract;
import com.jdelorenzo.hitthegym.model.Exercise;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditExerciseAdapter extends RecyclerView.Adapter<EditExerciseAdapter.ExerciseAdapterViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    final private ExerciseAdapterOnClickHandler mClickHandler;
    private View mEmptyView;
    private int lastPosition = -1;

    public EditExerciseAdapter(Context context, ExerciseAdapterOnClickHandler clickHandler,
                               View emptyView, int choiceMode) {
        mContext = context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
    }



    public interface ExerciseAdapterOnClickHandler {
        void onClick(Long id, Exercise exercise, ExerciseAdapterViewHolder vh);
        void onDelete(Long id, ExerciseAdapterViewHolder vh);
    }

    public class ExerciseAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        @BindView(R.id.delete_exercise_button) ImageButton deleteExerciseButton;
        @BindView(R.id.exercise_name) TextView exerciseName;
        @BindView(R.id.repetitions) TextView repetitions;
        @BindView(R.id.measurement) TextView weight;
        @BindView(R.id.sets) TextView sets;
        @BindView(R.id.list_item_add_exercise) View rootView;

        public ExerciseAdapterViewHolder(View view)
        {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int exerciseIdIndex = mCursor.getColumnIndex(WorkoutContract.ExerciseEntry._ID);
            int descriptionIndex = mCursor.getColumnIndex(WorkoutContract.ExerciseEntry.COLUMN_DESCRIPTION);
            int setsIndex = mCursor.getColumnIndex(WorkoutContract.ExerciseEntry.COLUMN_SETS);
            int repsIndex = mCursor.getColumnIndex(WorkoutContract.ExerciseEntry.COLUMN_REPS);
            int measurementIndex = mCursor.getColumnIndex(WorkoutContract.ExerciseEntry.COLUMN_MEASUREMENT);
            int measurementTypeIndex = mCursor.getColumnIndex(WorkoutContract.ExerciseEntry.COLUMN_MEASUREMENT_TYPE);
            @Exercise.MeasurementType int measurementType = mCursor.getInt(measurementTypeIndex);
            Exercise exercise = new Exercise(mCursor.getInt(setsIndex), mCursor.getInt(repsIndex),
                    mCursor.getString(descriptionIndex), mCursor.getDouble(measurementIndex),
                    measurementType);
            mClickHandler.onClick(mCursor.getLong(exerciseIdIndex), exercise, this);
        }

        @OnClick(R.id.delete_exercise_button)
        public void onDelete() {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_out_right);
            deleteExerciseButton.startAnimation(animation);
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            notifyItemRemoved(adapterPosition);
            int exerciseId = mCursor.getColumnIndex(WorkoutContract.ExerciseEntry._ID);
            mClickHandler.onDelete(mCursor.getLong(exerciseId), this);
        }
    }

    @Override
    public ExerciseAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_add_exercise, parent, false);
            view.setFocusable(true);
            return new ExerciseAdapterViewHolder(view);
        } else {
            throw new RuntimeException("ExerciseAdapter not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(final ExerciseAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.exerciseName.setText(mCursor.getString(EditWorkoutFragment.COL_DESCRIPTION));
        holder.repetitions.setText(String.format(Locale.getDefault(),
                mContext.getString(R.string.format_reps),
                mCursor.getInt(EditWorkoutFragment.COL_REPS)));
        holder.sets.setText(String.format(Locale.getDefault(),
                mContext.getString(R.string.format_sets),
                mCursor.getInt(EditWorkoutFragment.COL_SETS)));
        @Exercise.MeasurementType int measurementType = mCursor.getInt(EditWorkoutFragment.COL_MEASUREMENT_TYPE);
        holder.weight.setText(Utility.getFormattedMeasurementString(mContext,
                mCursor.getDouble(EditWorkoutFragment.COL_WEIGHT),
                measurementType));
        setAnimation(holder.rootView, position);
    }
    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            view.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }


}
