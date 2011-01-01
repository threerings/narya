//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

        public Comparable<?> getKey ()
        {
            return _value;
        }

        @Override
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

    @Override
    public void runTest ()
    {
        ArrayList<TestEntry> seed = new ArrayList<TestEntry>();
        seed.add(new TestEntry(15));
        seed.add(new TestEntry(7));
        seed.add(new TestEntry(3));
        seed.add(new TestEntry(29));
        seed.add(new TestEntry(32));

        DSet<TestEntry> set = new DSet<TestEntry>(seed.iterator());
        assertFalse(set.add(new TestEntry(15)));
        assertTrue(set.add(new TestEntry(9)));
        assertTrue(set.remove(new TestEntry(32)));
        assertFalse(set.remove(new TestEntry(32)));
        assertTrue(set.add(new TestEntry(32)));
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
