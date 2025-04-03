//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import org.junit.Test;

import com.threerings.presents.data.TestObject;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.ElementUpdateListener;
import com.threerings.presents.dobj.ElementUpdatedEvent;

import static org.junit.Assert.assertTrue;

/**
 * A simple test case for the dobjmgr.
 */
public class DOMTest extends PresentsTestBase
    implements AttributeChangeListener, ElementUpdateListener
{
    @Test public void runTest ()
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

    // from interface AttributeChangeListener
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

    // from interface ElementUpdateListener
    public void elementUpdated (ElementUpdatedEvent event)
    {
//         Log.info("Element updated " + event);
//         Log.info(StringUtil.toString(_test.ints));
//         Log.info(StringUtil.toString(_test.strings));
    }

    protected int _fcount = 0;
    protected TestObject _test;

    // the fields that will change in attribute changed events
    protected Object[] fields = { TestObject.FOO, TestObject.BAR, TestObject.FOO, TestObject.BAR };

    // the values we'll receive via attribute changed events
    protected Object[] values = { new Integer(99), "hoopie", new Integer(25), "howdy" };

    protected PresentsDObjectMgr _omgr = getInstance(PresentsDObjectMgr.class);
}
