package com.jdelorenzo.hitthegym.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.jdelorenzo.hitthegym.R;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SelectExerciseDialogFragment extends DialogFragment {
    private String[] mExercises;
    private long[] mExerciseIds;
    private Unbinder unbinder;
    @BindView(R.id.exercise_selector) AppCompatSpinner exerciseSelector;

    private static final String ARG_CALLBACK = "callback";
    private static final String ARG_EXERCISE_NAMES = "names";
    private static final String ARG_EXERCISE_IDS = "ids";

    public interface SelectExerciseListener extends Serializable {
        void onExerciseSelected(long exerciseId);
    }

    SelectExerciseListener mCallback;

    public static SelectExerciseDialogFragment newInstance(SelectExerciseListener callback, String[] workouts, long[] ids) {
        SelectExerciseDialogFragment selectExerciseDialogFragment = new SelectExerciseDialogFragment();
        Bundle b = new Bundle();
        b.putStringArray(ARG_EXERCISE_NAMES, workouts);
        b.putLongArray(ARG_EXERCISE_IDS, ids);
        b.putSerializable(ARG_CALLBACK, callback);
        selectExerciseDialogFragment.setArguments(b);
        return selectExerciseDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCallback = (SelectExerciseListener) getArguments().getSerializable(ARG_CALLBACK);
            mExercises = getArguments().getStringArray(ARG_EXERCISE_NAMES);
            mExerciseIds = getArguments().getLongArray(ARG_EXERCISE_IDS);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View rootView = inflater.inflate(R.layout.dialog_select_exercise, null);
        unbinder = ButterKnife.bind(this, rootView);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(),
                android.R.layout.simple_spinner_item, mExercises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSelector.setAdapter(adapter);
        builder.setTitle(R.string.dialog_select_exercise_title)
                .setView(rootView)
                .setPositiveButton(R.string.action_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        long selectedId = mExerciseIds[exerciseSelector.getSelectedItemPosition()];
                        mCallback.onExerciseSelected(selectedId);
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
}
