package eu.javimar.notitas.util;

import android.view.animation.Interpolator;

public class MyBounceInterpolator implements Interpolator
{
    private double mAmplitude;
    private double mFrequency;

    public MyBounceInterpolator(double amplitude, double frequency)
    {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }

    public float getInterpolation(float time)
    {
        return (float) (-1 * Math.pow(Math.E, -time/ mAmplitude) *
                Math.cos(mFrequency * time) + 1);
    }
}
