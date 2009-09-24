//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.util;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.samskivert.util.StringUtil;

import static com.threerings.NaryaLog.log;

/**
 * Records the times that it takes to call methods. Allows one simultaneous method call per thread
 * (no nested calls). Uses java's nano second timer. Results are logged by method name.
 */
public class MethodProfiler
{
    /**
     * The results of sampling for a single method.
     */
    public static class Result
    {
        /**
         * Creates a new result with the given values.
         */
        Result (int numSamples, double average, double stdDev)
        {
            this.numSamples = numSamples;
            this.averageTime = average;
            this.standardDeviation = stdDev;
        }

        /** Number of method calls sampled. */
        public int numSamples;

        /** Average time spent in the method. */
        public double averageTime;

        /** Standard deviation from the average. */
        public double standardDeviation;

        // from Object
        public String toString ()
        {
            return StringUtil.fieldsToString(this);
        }
    }

    /**
     * Runs some very basic tests of the method profiler.
     */
    public static void main (String args[])
        throws InterruptedException
    {
        int testNum = 0;
        if (args.length > 0) {
            testNum = Integer.parseInt(args[0]);
        }
        switch (testNum) {
        case 0: // rum some rpc threads
            MethodProfiler test = new MethodProfiler();
            Thread t1 = test.new TestThread("testm1", 100, 50);
            Thread t2 = test.new TestThread("testm2", 100, 50);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            for (Map.Entry<String, Result> method : test.getResults().entrySet()) {
                log.info(method.getKey(), "result", method.getValue());
            }
            break;
        case 1:
            simpleSampleTest("Single", 100);
            break;
        case 2:
            simpleSampleTest("Triple", 100, 0, 200);
            break;
        case 3:
            simpleSampleTest("Multi", 0, 25, 50, 100, 125, 150, 175, 200, 112.5, 112.5, 112.5);
            break;
        case 4:
            MethodProfiler test4 = new MethodProfiler();
            test4.enter("L1a");
            test4.enter("L2a");
            test4.swap("L2b");
            test4.enter("L3a");
            test4.exit(null);
            test4.exit(null);
            test4.swap("L1b");
            test4.swap("L1c");
            test4.exit(null);
            for (Map.Entry<String, Result> result : test4.getResults().entrySet()) {
                log.info("Results", "name", result.getKey(), "value", result.getValue());
            }
        }
    }

    /**
     * Creates a new profiler.
     */
    public MethodProfiler ()
    {
    }

    /**
     * Gets all method results so far.
     */
    public Map<String, Result> getResults ()
    {
        HashMap<String, MethodProfile> profiles = new HashMap<String, MethodProfile>();
        synchronized (_profiles) {
            profiles.putAll(_profiles);
        }

        HashMap<String, Result> results = new HashMap<String, Result>();
        for (Map.Entry<String, MethodProfile> entry : profiles.entrySet()) {
            synchronized (entry.getValue()) {
                results.put(entry.getKey(), entry.getValue().toResult());
            }
        }

        return results;
    }

    /**
     * Notes that the calling thread has entered the given method and records the time stamp.
     */
    public void enter (String methodName)
    {
        Method method = _stack.get().push();
        method.name = methodName;
        method.entryTime = System.nanoTime();
    }

    /**
     * Notes that the calling thread has exited the given method and records the time delta since
     * entry. The method parameter is not strictly necessary but allows some error checking. If not
     * null, it must match the most recent value given to {@link #enter} for the calling thread.
     */
    public void exit (String methodName)
    {
        long nanos = System.nanoTime();
        Method method = _stack.get().pop();
        if (method == null || (methodName != null && !methodName.equals(method.name))) {
            // TODO: warn, but only once
            return;
        }

        long elapsed = nanos - method.entryTime;
        recordTime(method.fullName(), (double)elapsed / 1000000);
        method.name = null;
    }

    /**
     * Transition to a new method or segment. Exits the current one and enters the given one.
     */
    public void swap (String methodName)
    {
        exit(null);
        enter(methodName);
    }

    /**
     * Clears out the profile for the current thread, and invokes the exit of the top-level method.
     * This allows callers to use one try... finally block in their top-level method without skewing
     * the results for nested methods that may have thrown exceptions and/or not called
     * {@link #exit}.
     */
    public void exitAndClear (String methodName)
    {
        Stack stack = _stack.get();
        while (stack.size() > 1) {
            stack.pop();
        }
        if (stack.size() > 0) {
            exit(methodName);
        }
    }

    /**
     * Clears all recorded methods and times.
     */
    public void reset ()
    {
        synchronized (_profiles) {
            _profiles.clear();
        }
    }

    /**
     * Adds a sample to our profile of the given method.
     */
    protected void recordTime (String method, double elapsedMs)
    {
        MethodProfile profile;
        synchronized (_profiles) {
            profile = _profiles.get(method);
            if (profile == null) {
                _profiles.put(method, profile = new MethodProfile());
            }
        }

        synchronized (profile) {
            profile.addSample(elapsedMs);
        }
    }

    /**
     * For testing, just calls the {@link #enter} and {@link #exit} methods at a fixed interval
     * for a given number of times.
     */
    protected class TestThread extends Thread
    {
        public TestThread (String method, int methodCount, long sleep)
        {
            _method = method;
            _methodCount = methodCount;
            _sleep = sleep;
        }

        // from Runnable
        @Override public void run ()
        {
            try {
                for (int i = 0; i < _methodCount; ++i) {
                    MethodProfiler.this.enter(_method);
                    Thread.sleep(_sleep);
                    MethodProfiler.this.exit(_method);
                }
            } catch (InterruptedException ie) {
            }
        }

        protected int _methodCount;
        protected String _method;
        protected long _sleep;
    }

    /**
     * Describes what we know about an in-progress method call.
     */
    protected static class Method
    {
        /** The name of the entered method. */
        public String name;

        /** The time the method was entered. */
        public long entryTime;

        /** The parent of the method. */
        public Method caller;

        /**
         * Gets this method's name, prefixed with all parent method names separated by dots.
         */
        public String fullName ()
        {
            if (caller != null) {
                return caller.fullName() + "." + name;
            }
            return name;
        }
    }

    /**
     * Describes what we know about a nested set of in progress method calls. This is a fake stack
     * that avoid reallocating entries, i.e. pop() == push() and push() == pop().
     */
    protected static class Stack
    {
        public Method push ()
        {
            if (_size == _methods.length) {
                Method []realloc = new Method[_size + 1];
                System.arraycopy(_methods, 0, realloc, 0, _size);
                _methods = realloc;
                _methods[_size] = new Method();
            }
            _methods[_size].name = null;
            _methods[_size].caller = _size > 0 ? _methods[_size - 1] : null;
            return _methods[_size++];
        }

        public Method pop ()
        {
            if (_size == 0) {
                return null;
            }
            return _methods[--_size];
        }

        public int size ()
        {
            return _size;
        }

        protected int _size;
        protected Method[] _methods = {new Method()};
    }

    /**
     * Holds the aggregate information about all the calls to a given method. The caller is
     * expected to perform all thread coordination.
     */
    protected static class MethodProfile
    {
        /**
         * Records a new elapsed time spent in the method.
         */
        public void addSample (double time)
        {
            if (_numSamples == 0) {
                _numSamples = 1;
                _average = time;
                _variance = 0;
                return;
            }
            _numSamples = _numSamples + 1;
            double oldAverage = _average;
            _average += (time - _average) / _numSamples;
            double baseVariance = time - oldAverage;
            _variance += (_numSamples - 1) * baseVariance * baseVariance / _numSamples;
        }

        /**
         * Calculates the results of the the profile.
         */
        public Result toResult ()
        {
            return new Result(_numSamples, _average, Math.sqrt(_variance / _numSamples));
        }

        protected int _numSamples;
        protected double _average;
        protected double _variance;
    }

    /**
     * Runs the method profile stat collection with the given samples and logs the result.
     */
    protected static void simpleSampleTest (String name, double... samples)
    {
        MethodProfile prof = new MethodProfile();
        for (double sample : samples) {
            prof.addSample(sample);
        }
        log.info(name, "results", prof.toResult());
    }

    /** Set of active methods in the current thread. */
    protected ThreadLocal<Stack> _stack = new ThreadLocal<Stack>() {
        @Override protected Stack initialValue () {
            return new Stack();
        }
    };

    /** Profiles by method name. */
    protected HashMap<String, MethodProfile> _profiles = Maps.newHashMap();
}
