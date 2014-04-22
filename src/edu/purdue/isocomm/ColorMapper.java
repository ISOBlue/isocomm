package edu.purdue.isocomm;

import android.graphics.Color;

public class ColorMapper<E extends Number> {
    
    public final E mMin, mMax;
    public final float mSat, mVal;

    public ColorMapper(E min, E max, float saturation, float value) {
        mMin = min;
        mMax = max;
        mSat = saturation;
        mVal = value;
    }

    public ColorMapper(E min, E max) {
        this(min, max, 1.0f, 1.0f);
    }
    
    public int map(E num) {
        float[] hsv = new float[3];

        hsv[2] = mVal;
        hsv[1] = mSat;
        hsv[0] = (float) (120.0 / mMax.doubleValue() * (num.doubleValue() - mMin.doubleValue()));

        return Color.HSVToColor(hsv);
    }
}
