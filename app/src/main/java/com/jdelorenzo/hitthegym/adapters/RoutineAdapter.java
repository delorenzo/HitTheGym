package com.jdelorenzo.hitthegym.adapters;

import android.content.Context;
import android.database.Cursor;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.jdelorenzo.hitthegym.EditRoutineFragment;
import com.jdelorenzo.hitthegym.R;
import com.jdelorenzo.hitthegym.data.WorkoutContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineAdapterViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    final private RoutineAdapterOnClickHandler mClickHandler;
    private View mEmptyView;

    public RoutineAdapter(Context context, RoutineAdapterOnClickHandler clickHandler,
                          View emptyView) {
        mContext = context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
    }

    public interface RoutineAdapterOnClickHandler {
        void onClick(Long id, RoutineAdapterViewHolder vh);
        void onDelete(Long id, RoutineAdapterViewHolder vh);
    }

    public class RoutineAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.delete_routine_button) ImageButton deleteButton;
        @BindView(R.id.routine) Button routineButton;

        public RoutineAdapterViewHolder(View view)
        {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.day)
        public void onClick() {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int idIndex = mCursor.getColumnIndex(WorkoutContract.RoutineEntry._ID);
            mClickHandler.onClick(mCursor.getLong(idIndex), this);
        }

        @OnClick(R.id.delete_day_button)
        public void onDelete() {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dayId = mCursor.getColumnIndex(WorkoutContract.DayEntry._ID);
            mClickHandler.onDelete(mCursor.getLong(dayId), this);
        }
    }

    @Override
    public RoutineAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_day, parent, false);
            view.setFocusable(true);
            return new RoutineAdapterViewHolder(view);
        } else {
            throw new RuntimeException("RoutineAdapter not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(final RoutineAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.routineButton.setText(mCursor.getString(EditRoutineFragment.COL_NAME));
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
