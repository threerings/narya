package com.threerings.util;

/**
 * Calculates live values for the mean, variance and standard deviation of a set of samples.
 * <em>Not thread safe!</em>
 */
public class RunningStats
{
    /**
     * Adds a new sample.
     */
    public void addSample (double sample)
    {
        // From http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#On-line_algorithm
        _numSamples++;
        double deltaToOld = sample - _mean;
        _mean += deltaToOld / _numSamples;
        double deltaToNew = sample - _mean;
        _varianceSum += deltaToOld * deltaToNew;
        if (sample < _min) {
            _min = sample;
        }
        if (sample > _max) {
            _max = sample;
        }
    }

    /**
     * Returns the minimum sample added or {@link Double#POSITIVE_INFINITY} if no samples have
     * been added.
     */
    public double getMin ()
    {
        return _min;
    }

    /**
     * Returns the maximum sample added or {@link Double#NEGATIVE_INFINITY} if no samples have
     * been added.
     */
    public double getMax ()
    {
        return _max;
    }

    public double getVariance ()
    {
        if (getNumSamples() == 0) {
            return 0;
        }
        return _varianceSum / getNumSamples();
    }

    public int getNumSamples ()
    {
        return _numSamples;
    }

    public double getMean ()
    {
        return _mean;
    }

    public double getStandardDeviation ()
    {
        return Math.sqrt(getVariance());
    }

    protected int _numSamples;
    protected double _mean;
    protected double _varianceSum;
    protected double _max = Double.NEGATIVE_INFINITY, _min = Double.POSITIVE_INFINITY;
}