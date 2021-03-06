package com.jdelorenzo.hitthegym.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jdelorenzo.hitthegym.R;
import com.jdelorenzo.hitthegym.Utility;
import com.jdelorenzo.hitthegym.model.Exercise;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/*
This fragment is used to create a new exercise.
The CreateExerciseDialogFragmentListener callback can be eithehr passed in on newInstance() or
implemented in the calling activity.
 */
public class CreateExerciseDialogFragment extends DialogFragment {
    private static final String ARG_CALLBACK = "callback";
    private static final String ARG_EXERCISE = "exercise";
    private static final String ARG_EXERCISES_EXIST = "exercisesExist";
    @BindView(R.id.exercise) EditText exerciseEditText;
    @BindView(R.id.sets) EditText setsEditText;
    @BindView(R.id.repetitions) EditText repetitionsEditText;
    @BindView(R.id.measurement) EditText measurementEditText;
    @BindView(R.id.measurement_units) TextView measurementUnits;
    @BindView(R.id.measurement_selector) AppCompatSpinner measurementSelector;
    private @Exercise.MeasurementType int measurementType;
    private static final String LOG_TAG = CreateExerciseDialogFragment.class.getSimpleName();
    private Exercise mExercise;
    private boolean mExercisesExist;

    private Unbinder unbinder;
    public interface CreateExerciseDialogFragmentListener extends Serializable {
        void onCreateExercise(Exercise exercise);
        void onSelectExisting();
    }

    private CreateExerciseDialogFragmentListener mCallback;

    public static CreateExerciseDialogFragment newInstance(boolean exercisesExist, CreateExerciseDialogFragmentListener callback) {
        Bundle b = new Bundle();
        b.putSerializable(ARG_CALLBACK, callback);
        b.putBoolean(ARG_EXERCISES_EXIST, exercisesExist);
        CreateExerciseDialogFragment fragment = new CreateExerciseDialogFragment();
        fragment.setArguments(b);
        return fragment;
    }

    public static CreateExerciseDialogFragment newInstance(Exercise exercise, CreateExerciseDialogFragmentListener callback) {
        Bundle b = new Bundle();
        b.putSerializable(ARG_CALLBACK, callback);
        b.putParcelable(ARG_EXERCISE, exercise);
        CreateExerciseDialogFragment fragment = new CreateExerciseDialogFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mCallback = (CreateExerciseDialogFragmentListener) args.getSerializable(ARG_CALLBACK);
            mExercise = args.getParcelable(ARG_EXERCISE);
            mExercisesExist = args.getBoolean(ARG_EXERCISES_EXIST, false);
        }

        //fill in the exercise if it was chosen from an existing exercise
        if (mExercise != null) {
            exerciseEditText.setText(mExercise.getDescription());
            setsEditText.setText(mExercise.getSets());
            repetitionsEditText.setText(mExercise.getReps());
            measurementEditText.setText(Utility.getFormattedWeightStringWithoutUnits(getActivity(),
                    mExercise.getMeasurement()));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View rootView = inflater.inflate(R.layout.dialog_add_exercise, null);
        unbinder = ButterKnife.bind(this, rootView);
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
        builder.setTitle(R.string.dialog_create_exercise_title)
                .setIcon(R.drawable.ic_run_color_primary_24dp)
                .setMessage(R.string.dialog_create_exercise_text)
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
        if (mExercisesExist) {
            builder.setNeutralButton(getString(R.string.dialog_use_existing_exercise), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mCallback.onSelectExisting();
                    dismiss();
                }
            });
        }
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog)getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String description = exerciseEditText.getText().toString();
                    if (description.isEmpty()) {
                        exerciseEditText.setError(getActivity().getString(R.string.error_message_exercise_name));
                        return;
                    }
                    else if (!description.matches(getString(R.string.regex_valid_name))) {
                        exerciseEditText.setError(getString(R.string.error_message_invalid_name));
                        return;
                    }
                    String setText = setsEditText.getText().toString();
                    if (setText.isEmpty()) {
                        setsEditText.setError(getActivity().getString(R.string.error_message_sets));
                        return;
                    }
                    else if (!setText.matches(getString(R.string.regex_valid_digit))) {
                        setsEditText.setError(getString(R.string.error_message_invalid_digit));
                        return;
                    }
                    int sets = Integer.parseInt(setText);
                    String repetitionText = repetitionsEditText.getText().toString();
                    if (repetitionText.isEmpty()) {
                        repetitionsEditText.setError(getActivity().getString(R.string.error_message_repetitions));
                        return;
                    }
                    else if (!repetitionText.matches(getString(R.string.regex_valid_digit))) {
                        repetitionsEditText.setError(getString(R.string.error_message_invalid_digit));
                        return;
                    }
                    int repetitions = Integer.parseInt(repetitionText);
                    String measurementText = measurementEditText.getText().toString();
                    double measurement;
                    if (measurementText.isEmpty()) {
                        measurementEditText.setError(getString(R.string.error_message_field_required));
                        return;
                    }
                    if (!measurementText.matches(getString(R.string.regex_valid_decimal))) {
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
                    mCallback.onCreateExercise(exercise);
                    dismiss();
                }
            });
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getArguments() != null) {
            mCallback = (CreateExerciseDialogFragmentListener) getArguments().getSerializable(ARG_CALLBACK);
        }
        try {
            if (null == mCallback) {
                mCallback = (CreateExerciseDialogFragmentListener) activity;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement CreateExerciseDialogFragmentListener");
        }
    }
}
