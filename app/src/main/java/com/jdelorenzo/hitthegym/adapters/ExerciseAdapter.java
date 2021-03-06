package com.jdelorenzo.hitthegym.adapters;

import android.content.Context;
import android.database.Cursor;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jdelorenzo.hitthegym.R;
import com.jdelorenzo.hitthegym.Utility;
import com.jdelorenzo.hitthegym.WorkoutFragment;
import com.jdelorenzo.hitthegym.data.WorkoutContract;
import com.jdelorenzo.hitthegym.model.Exercise;
import com.jdelorenzo.hitthegym.model.Weight;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseAdapterViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    final private ExerciseAdapterOnClickHandler mClickHandler;
    private View mEmptyView;
    private View mCompletedView;
    private int itemsChecked;
    private boolean completed;
    private int lastPosition = -1;

    public ExerciseAdapter(Context context, ExerciseAdapterOnClickHandler clickHandler,
                           View emptyView, View completedView, int choiceMode) {
        mContext = context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
        mCompletedView = completedView;
    }

    public interface ExerciseAdapterOnClickHandler {
        void onClick(long id, double weight, @Exercise.MeasurementType int measurementType,
                     ExerciseAdapterViewHolder vh);
        void allItemsChecked();
    }

    public class ExerciseAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        @BindView(R.id.list_item_exercise) View rootView;
        @BindView(R.id.complete_checkbox) CheckBox completeCheckbox;
        @BindView(R.id.exercise_name) TextView exerciseName;
        @BindView(R.id.repetitions) TextView repetitions;
        @BindView(R.id.measurement) TextView weight;
        @BindView(R.id.sets) TextView sets;
        private int setCount;

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
            int exerciseIndex = mCursor.getColumnIndex(WorkoutContract.ExerciseEntry._ID);
            int measurementIndex = mCursor.getColumnIndex(WorkoutContract.ExerciseEntry.COLUMN_MEASUREMENT);
            int measurementTypeIndex = mCursor.getColumnIndex(WorkoutContract.ExerciseEntry.COLUMN_MEASUREMENT_TYPE);
            @Exercise.MeasurementType int measurementType = mCursor.getInt(measurementIndex);
            mClickHandler.onClick(mCursor.getLong(exerciseIndex),
                    mCursor.getDouble(measurementIndex),
                    measurementType,
                    this);
        }
    }

    @Override
    public ExerciseAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exercise, parent, false);
            view.setFocusable(true);
            return new ExerciseAdapterViewHolder(view);
        } else {
            throw new RuntimeException("ExerciseAdapter not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(final ExerciseAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.exerciseName.setText(mCursor.getString(WorkoutFragment.COL_DESCRIPTION));
        holder.repetitions.setText(String.format(Locale.getDefault(), mContext.getString(R.string.format_reps),
                mCursor.getInt(WorkoutFragment.COL_REPS)));
        holder.setCount = mCursor.getInt(WorkoutFragment.COL_SETS);
        holder.sets.setText(String.format(Locale.getDefault(), mContext.getString(R.string.format_sets), holder.setCount));
        @Exercise.MeasurementType int measurementType = mCursor.getInt(WorkoutFragment.COL_MEASUREMENT_TYPE);
        holder.weight.setText(Utility.getFormattedMeasurementString(mContext,
                mCursor.getDouble(WorkoutFragment.COL_MEASUREMENT), measurementType));
        if (completed) {
            holder.completeCheckbox.setChecked(true);
            holder.completeCheckbox.setClickable(false);
            holder.completeCheckbox.setAlpha(0.5f);
            holder.rootView.setClickable(false);
            int disabledColor = ContextCompat.getColor(mContext, R.color.disabled_text);
            holder.exerciseName.setTextColor(disabledColor);
            holder.repetitions.setTextColor(disabledColor);
            holder.sets.setTextColor(disabledColor);
            holder.weight.setTextColor(disabledColor);
        }
        else {
            holder.completeCheckbox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked && holder.setCount > 0) {
                                holder.setCount--;
                                holder.sets.setText(String.format(Locale.getDefault(),
                                        mContext.getString(R.string.format_sets),
                                        holder.setCount));
                            }
                            if (holder.setCount > 0) {
                                buttonView.setChecked(false);
                            } else {
                                itemsChecked++;
                                Log.e("ExerciseAdapter", itemsChecked + ":  " + mCursor.getCount());
                                if (itemsChecked >= mCursor.getCount()) {
                                    mClickHandler.allItemsChecked();
                                }
                            }
                        }
                    });
        }
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
        //check to see if the workout has been completed by checking its last date against
        //the current date.  display completed text if that's the case.
        if (getItemCount() > 0 && newCursor.moveToFirst()) {
            mEmptyView.setVisibility(View.GONE);
            String lastDateString = null;
            if (!newCursor.isNull(WorkoutFragment.COL_LAST_DATE)) {
                lastDateString = newCursor.getString(WorkoutFragment.COL_LAST_DATE);
            }
            LocalDate date = new LocalDate();
            if (lastDateString != null && lastDateString.equals(
                    date.toString(mContext.getString(R.string.date_format)))) {
                mCompletedView.setVisibility(View.VISIBLE);
                completed = true;
            } else {
                mCompletedView.setVisibility(View.GONE);
                              completed = false;
            }
        }
        else {
            mCompletedView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public long getDayId() {
        if (mCursor != null && mCursor.moveToFirst()) {
            return mCursor.getLong(WorkoutFragment.COL_DAY_ID);
        }
        return 0;
    }

    public ArrayList<Weight> getWeights() {
        ArrayList<Weight> weights = new ArrayList<>();
        if (mCursor != null && mCursor.moveToFirst()) {
            while (!mCursor.isAfterLast()) {
                weights.add(new Weight(
                        mCursor.getLong(WorkoutFragment.COL_EXERCISE_ID),
                        mCursor.getDouble(WorkoutFragment.COL_MEASUREMENT)
                ));
                mCursor.moveToNext();
            }
        }
        return weights;
    }


}
