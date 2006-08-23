//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.server;

import junit.framework.Test;
import junit.framework.TestCase;

import com.threerings.presents.data.TestObject;
import com.threerings.presents.dobj.*;

/**
 * A simple test case for the dobjmgr.
 */
public class DOMTest extends TestCase
    implements AttributeChangeListener, ElementUpdateListener
{
    public DOMTest ()
    {
        super(DOMTest.class.getName());
    }

    public void attributeChanged (AttributeChangedEvent event)
    {
        assertTrue(fields[_fcount] + " == " + values[_fcount],
                   event.getName().equals(fields[_fcount]) &&
                   event.getValue().equals(values[_fcount]));

        // shutdown once we receive our last update
        if (++_fcount == fields.length) {
            _omgr.harshShutdown();
        }
    }

    public void elementUpdated (ElementUpdatedEvent event)
    {
//         Log.info("Element updated " + event);
//         Log.info(StringUtil.toString(_test.ints));
//         Log.info(StringUtil.toString(_test.strings));
    }

    public void runTest ()
    {
        // request that a new TestObject be registered
        _test = _omgr.registerObject(new TestObject());

        // add ourselves as a listener
        _test.addListener(this);

        // test transactions
        _test.startTransaction();
        _test.setFoo(99);
        _test.setBar("hoopie");
        _test.commitTransaction();

        // set some elements
        _test.setIntsAt(15, 3);
        _test.setIntsAt(5, 2);
        _test.setIntsAt(1, 0);
        _test.setStringsAt("Hello", 0);
        _test.setStringsAt("Goodbye", 1);
        _test.setStringsAt(null, 1);

        // now set some values straight up
        _test.setFoo(25);
        _test.setBar("howdy");

        // and run the object manager
        _omgr.run();
    }

    public static Test suite ()
    {
        return new DOMTest();
    }

    public static void main (String[] args)
    {
        DOMTest test = new DOMTest();
        test.runTest();
    }

    protected int _fcount = 0;
    protected TestObject _test;

    // the fields that will change in attribute changed events
    protected Object[] fields = {
        TestObject.FOO, TestObject.BAR, TestObject.FOO, TestObject.BAR };

    // the values we'll receive via attribute changed events
    protected Object[] values = {
        new Integer(99), "hoopie", new Integer(25), "howdy" };

    protected static PresentsDObjectMgr _omgr = new PresentsDObjectMgr();
}
