package com.jdelorenzo.hitthegym;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jdelorenzo.hitthegym.model.Exercise;

import java.util.Locale;

public class Utility {

    public static String getFormattedMeasurementString(Context context, double measurement,
                                                       @Exercise.MeasurementType int measurementType) {
        if (measurementType == Exercise.MEASUREMENT_TYPE_WEIGHT) {
            return getFormattedWeightString(context, measurement);
        }
        else {
            return String.format(Locale.getDefault(), context.getString(R.string.duration_format),
                    measurement);
        }
    }

    public static String getFormattedMeasurementStringWithoutUnits(Context context, double measurement,
                                                       @Exercise.MeasurementType int measurementType) {
        if (measurementType == Exercise.MEASUREMENT_TYPE_WEIGHT) {
            return getFormattedWeightStringWithoutUnits(context, measurement);
        }
        else {
            return String.valueOf(measurement);
        }
    }

    public static String getFormattedWeightString(Context context, double weight) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = preferences.getString(context.getString(R.string.prefs_weight_unit_key),
                context.getString(R.string.prefs_weight_unit_default));
        if (unit.equals(context.getString(R.string.prefs_weight_unit_imperial_value))) {
            weight = toImperial(weight);
            return String.format(Locale.getDefault(),
                    context.getString(R.string.weight_format_imperial), weight);
        }
        else {
            return String.format(Locale.getDefault(),
                    context.getString(R.string.weight_format_metric), weight);
        }
    }

    public static String getFormattedWeightStringWithoutUnits(Context context, double weight) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = preferences.getString(context.getString(R.string.prefs_weight_unit_key),
                context.getString(R.string.prefs_weight_unit_default));
        if (unit.equals(context.getString(R.string.prefs_weight_unit_imperial_value))) {
            weight = toImperial(weight);
        }
        return String.format(Locale.getDefault(), context.getString(R.string.weight_format), weight);
    }

    public static String getWeightUnits(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = preferences.getString(context.getString(R.string.prefs_weight_unit_key),
                context.getString(R.string.prefs_weight_unit_default));
        if (unit.equals(context.getString(R.string.prefs_weight_unit_imperial_value))) {
            return context.getString(R.string.weight_units_imperial);
        }
        else {
            return context.getString(R.string.weight_units_metric);
        }
    }

    public static double convertWeightToMetric(Context context, double weight) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = preferences.getString(context.getString(R.string.prefs_weight_unit_key),
                context.getString(R.string.prefs_weight_unit_default));
        if (unit.equals(context.getString(R.string.prefs_weight_unit_imperial_value))) {
            return toMetric(weight);
        }
        else {
            return weight;
        }
    }

    public static double convertWeightToImperial(Context context, double weight) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = preferences.getString(context.getString(R.string.prefs_weight_unit_key),
                context.getString(R.string.prefs_weight_unit_default));
        if (unit.equals(context.getString(R.string.prefs_weight_unit_imperial_value))) {
            return weight;
        }
        else {
            return toImperial(weight);
        }
    }

    public static double convertWeight(Context context, double weight) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = preferences.getString(context.getString(R.string.prefs_weight_unit_key),
                context.getString(R.string.prefs_weight_unit_default));
        if (unit.equals(context.getString(R.string.prefs_weight_unit_imperial_value))) {
            return toImperial(weight);
        }
        else {
            return weight;
        }
    }

    private static double toImperial(double kg) {
        return kg * 2.205;
    }

    private static double toMetric (double lb) {
        return lb / 2.205;
    }
}
