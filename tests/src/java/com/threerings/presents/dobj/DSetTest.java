//
// $Id: DSetTest.java,v 1.1 2002/08/14 19:08:00 mdb Exp $

package com.threerings.presents.dobj;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests the {@link DSet} class.
 */
public class DSetTest extends TestCase
{
    public static class TestEntry implements DSet.Entry
    {
        public TestEntry (int value)
        {
            _value = new Integer(value);
        }

        public Comparable getKey ()
        {
            return _value;
        }

        public String toString ()
        {
            return _value.toString();
        }

        protected Integer _value;
    }

    public DSetTest ()
    {
        super(DSetTest.class.getName());
    }

    public void runTest ()
    {
        ArrayList seed = new ArrayList();
        seed.add(new TestEntry(15));
        seed.add(new TestEntry(7));
        seed.add(new TestEntry(3));
        seed.add(new TestEntry(29));
        seed.add(new TestEntry(32));

        DSet set = new DSet(seed.iterator());
        System.out.println(set.add(new TestEntry(15)) + ": " + set);
        System.out.println(set.add(new TestEntry(9)) + ": " + set);
        System.out.println(set.remove(new TestEntry(32)) + ": " + set);
        System.out.println(set.add(new TestEntry(32)) + ": " + set);
    }

    public static Test suite ()
    {
        return new DSetTest();
    }

    public static void main (String[] args)
    {
        DSetTest test = new DSetTest();
        test.runTest();
    }
}
