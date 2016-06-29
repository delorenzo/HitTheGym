package com.jdelorenzo.hitthegym.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.jdelorenzo.hitthegym.R;
import com.jdelorenzo.hitthegym.Utility;
import com.jdelorenzo.hitthegym.model.Exercise;

import java.io.Serializable;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/*
This fragment is used to create a new exercise.
The CreateExerciseDialogFragmentListener callback can be either passed in on newInstance() or
implemented in the calling activity.
 */
public class EditExerciseDialogFragment extends DialogFragment {
    private static final String ARG_CALLBACK = "callback";
    private static final String ARG_EXERCISE = "exercise";
    private static final String LOG_TAG = EditExerciseDialogFragment.class.getSimpleName();
    private @Exercise.MeasurementType int measurementType;

    @BindView(R.id.exercise) EditText exerciseEditText;
    @BindView(R.id.sets) EditText setsEditText;
    @BindView(R.id.repetitions) EditText repetitionsEditText;
    @BindView(R.id.measurement) EditText measurementEditText;
    @BindView(R.id.measurement_units) TextView measurementUnits;
    @BindView(R.id.measurement_selector) AppCompatSpinner measurementSelector;

    private Unbinder unbinder;
    public interface EditExerciseDialogFragmentListener extends Serializable {
        void onEditExercise(Exercise exercise);
    }

    private EditExerciseDialogFragmentListener mCallback;

    public static EditExerciseDialogFragment newInstance(Exercise exercise,
                                                         EditExerciseDialogFragmentListener callback) {
        Bundle b = new Bundle();
        b.putParcelable(ARG_EXERCISE, exercise);
        b.putSerializable(ARG_CALLBACK, callback);
        EditExerciseDialogFragment fragment = new EditExerciseDialogFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View rootView = inflater.inflate(R.layout.dialog_add_exercise, null);
        unbinder = ButterKnife.bind(this, rootView);
        if (args != null) {
            Exercise exercise = args.getParcelable(ARG_EXERCISE);
            mCallback = (EditExerciseDialogFragmentListener) args.getSerializable(ARG_CALLBACK);
            exerciseEditText.setText(exercise.getDescription());
            repetitionsEditText.setText(String.format(Locale.getDefault(), "%d", exercise.getReps()));
            setsEditText.setText(String.format(Locale.getDefault(), "%d", exercise.getSets()));
            String weightString = Utility.getFormattedMeasurementStringWithoutUnits(getActivity(),
                    exercise.getMeasurement(), exercise.getMeasurementType());
            measurementEditText.setText(weightString);
            measurementType = exercise.getMeasurementType();
        }
        measurementUnits.setText(Utility.getWeightUnits(getActivity()));
        measurementSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String) adapterView.getItemAtPosition(i);
                if (selection.equals(getString(R.string.measurement_duration))) {
                    measurementUnits.setText(getString(R.string.duration_units));
                    measurementType = Exercise.MEASUREMENT_TYPE_DURATION;
                }
                else {
                    measurementUnits.setText(Utility.getWeightUnits(getActivity()));
                    measurementType = Exercise.MEASUREMENT_TYPE_WEIGHT;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        measurementSelector.setSelection(measurementType);
        builder.setTitle(R.string.dialog_edit_exercise_title)
                .setView(rootView)
                .setPositiveButton(R.string.action_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                        Do nothing here because the button is overridden to change the closing behavior.
                         */
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
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog)getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String description = exerciseEditText.getText().toString();
                    if (description.isEmpty()) {
                        FirebaseCrash.logcat(Log.INFO, LOG_TAG, "Empty description entered");
                        exerciseEditText.setError(getActivity().getString(R.string.error_message_exercise_name));
                        return;
                    }
                    else if (!description.matches(getString(R.string.regex_valid_name))) {
                        FirebaseCrash.logcat(Log.INFO, LOG_TAG, "Invalid exercise name " + description + " entered");
                        exerciseEditText.setError(getString(R.string.error_message_invalid_name));
                        return;
                    }
                    String setText = setsEditText.getText().toString();
                    if (setText.isEmpty()) {
                        FirebaseCrash.logcat(Log.INFO, LOG_TAG, "Empty sets entered");
                        setsEditText.setError(getActivity().getString(R.string.error_message_sets));
                        return;
                    }
                    else if (!setText.matches(getString(R.string.regex_valid_digit))) {
                        FirebaseCrash.logcat(Log.INFO, LOG_TAG, "Invalid set amount " + setText + " entered");
                        setsEditText.setError(getString(R.string.error_message_invalid_digit));
                        return;
                    }
                    int sets = Integer.parseInt(setText);
                    String repetitionText = repetitionsEditText.getText().toString();
                    if (repetitionText.isEmpty()) {
                        FirebaseCrash.logcat(Log.INFO, LOG_TAG, "Empty reps entered");
                        repetitionsEditText.setError(getActivity().getString(R.string.error_message_repetitions));
                        return;
                    }
                    else if (!repetitionText.matches(getString(R.string.regex_valid_digit))) {
                        FirebaseCrash.logcat(Log.INFO, LOG_TAG, "Invalid rep amount " + repetitionText + " entered");
                        repetitionsEditText.setError(getString(R.string.error_message_invalid_digit));
                        return;
                    }
                    int repetitions = Integer.parseInt(repetitionText);
                    String measurementText = measurementEditText.getText().toString();
                    @Exercise.MeasurementType int measurementType = measurementSelector.getSelectedItemPosition();
                    double measurement ;
                    if (measurementText.isEmpty()) {
                        FirebaseCrash.logcat(Log.INFO, LOG_TAG, "Empty measurement entered");
                        measurementEditText.setError(getString(R.string.error_field_required));
                        return;
                    }
                    if (!measurementText.matches(getString(R.string.regex_valid_decimal))) {
                        FirebaseCrash.logcat(Log.INFO, LOG_TAG,
                                "Invalid measurement " + measurementText + " entered.");
                        measurementEditText.setError(getString(R.string.error_message_invalid_measurement));
                        return;
                    }
                    measurement =  Double.parseDouble(measurementText);
                    //always store weight in metric
                    if (measurementType == Exercise.MEASUREMENT_TYPE_WEIGHT) {
                        measurement = Utility.convertWeightToMetric(getActivity(), measurement);
                    }
                    Exercise exercise = new Exercise(
                            sets,
                            repetitions,
                            description,
                            measurement,
                            measurementType
                    );
                    mCallback.onEditExercise(exercise);
                    dismiss();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getArguments() != null) {
            mCallback = (EditExerciseDialogFragmentListener) getArguments().getSerializable(ARG_CALLBACK);
        }
        try {
            if (null == mCallback) {
                mCallback = (EditExerciseDialogFragmentListener) activity;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement CreateExerciseDialogFragmentListener");
        }
    }
}
