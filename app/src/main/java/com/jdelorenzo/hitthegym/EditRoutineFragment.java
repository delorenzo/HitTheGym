package com.jdelorenzo.hitthegym;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jdelorenzo.hitthegym.adapters.RoutineAdapter;
import com.jdelorenzo.hitthegym.data.WorkoutContract;
import com.jdelorenzo.hitthegym.service.DatabaseIntentService;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditRoutineFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_CALLBACK = "callback";
    private RoutineAdapter mAdapter;
    @BindView(R.id.empty_routine_textview) TextView mEmptyView;
    @BindView(R.id.add_routine_recyclerview) RecyclerView mRecyclerView;
    private EditRoutineListener mCallback;
    private Unbinder unbinder;
    private static final String FTAG_DIALOG_FRAGMENT = "dialogFragment";

    private static final int ROUTINE_LOADER = 0;

    private static final String[] ROUTINE_COLUMNS = {
            WorkoutContract.RoutineEntry.TABLE_NAME + "." + WorkoutContract.RoutineEntry._ID,
            WorkoutContract.RoutineEntry.COLUMN_NAME
    };

    public static final int COL_ROUTINE_ID = 0;
    public static final int COL_NAME = 1;

    public interface EditRoutineListener extends Serializable {
        void onRoutineSelected(long id);
        void onNoRoutineExists();
    }

    public static EditRoutineFragment newInstance(EditRoutineListener listener) {
        EditRoutineFragment fragment = new EditRoutineFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_CALLBACK, listener);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(ROUTINE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    //This method is never called on older APIs ( < 22 )
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getArguments() != null) {
            mCallback = (EditRoutineListener) getArguments().getSerializable(ARG_CALLBACK);
        }
        try {
            if (null == mCallback) {
                mCallback = (EditRoutineListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement SelectDayListener");
        }
    }

    //This deprecated method left in place to support older APIs
    @Override
    @SuppressWarnings("deprecated")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            if (null == mCallback) {
                mCallback = (EditRoutineListener) activity;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement SelectDayListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_routine, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new RoutineAdapter(getActivity(), new RoutineAdapter.RoutineAdapterOnClickHandler() {
            @Override
            public void onClick(Long id, RoutineAdapter.RoutineAdapterViewHolder vh) {
                mCallback.onRoutineSelected(id);
            }

            @Override
            public void onDelete(Long id, RoutineAdapter.RoutineAdapterViewHolder vh) {
                DatabaseIntentService.startActionDeleteRoutine(getActivity(), id);
                getActivity().getContentResolver().notifyChange(WorkoutContract.RoutineEntry.CONTENT_URI, null);
            }
        }, mEmptyView);
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
        return new CursorLoader(getActivity(),
                WorkoutContract.RoutineEntry.CONTENT_URI,
                ROUTINE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() == 0) {
            mCallback.onNoRoutineExists();
        }
        if (data != null && data.getCount() == 1) {
            long id = data.getLong(COL_ROUTINE_ID);
            data.close();
            mCallback.onRoutineSelected(id);
        }
        else {
            mAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
