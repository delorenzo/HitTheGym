package com.jdelorenzo.hitthegym.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Exercise implements Parcelable {
    int sets;
    int reps;
    String description;
    double measurement;
    @MeasurementType int measurementType;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MEASUREMENT_TYPE_WEIGHT, MEASUREMENT_TYPE_DURATION})
    public @interface MeasurementType {}
    public static final int MEASUREMENT_TYPE_WEIGHT = 0;
    public static final int MEASUREMENT_TYPE_DURATION = 1;

    public Exercise(int sets, int reps, String description, double weight,
                    @MeasurementType int measurementType) {
        this.sets = sets;
        this.reps = reps;
        this.description = description;
        this.measurement = weight;
        this.measurementType = measurementType;
    }

    public int getSets() {
        return sets;
    }

    public int getReps() {
        return reps;
    }

    public String getDescription() {
        return description;
    }

    public double getMeasurement() {
        return measurement;
    }

    @MeasurementType public int getMeasurementType() { return measurementType; }

    @SuppressWarnings("ResourceType")
    protected Exercise(Parcel in) {
        sets = in.readInt();
        reps = in.readInt();
        description = in.readString();
        measurement = in.readDouble();
        measurementType = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sets);
        dest.writeInt(reps);
        dest.writeString(description);
        dest.writeDouble(measurement);
        dest.writeInt(measurementType);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Exercise> CREATOR = new Parcelable.Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };
}