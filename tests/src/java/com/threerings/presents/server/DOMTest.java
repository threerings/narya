//
// $Id: DOMTest.java,v 1.7 2002/02/09 07:50:04 mdb Exp $

package com.threerings.presents.server;

import junit.framework.Test;
import junit.framework.TestCase;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;

/**
 * A simple test case for the dobjmgr.
 */
public class DOMTest extends TestCase
    implements Subscriber, AttributeChangeListener
{
    public DOMTest ()
    {
        super(DOMTest.class.getName());
    }

    public void objectAvailable (DObject object)
    {
        // add ourselves as a listener
        object.addListener(this);

        TestObject to = (TestObject)object;

        // test transactions
        to.startTransaction();
        to.setFoo(99);
        to.setBar("hoopie");
        to.commitTransaction();

        // now set some values straight up
        to.setFoo(25);
        to.setBar("howdy");
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        fail("Request failed: " + cause);
        _omgr.shutdown();
    }

    public void attributeChanged (AttributeChangedEvent event)
    {
        assert(fields[_fcount] + " == " + values[_fcount],
               event.getName().equals(fields[_fcount]) &&
               event.getValue().equals(values[_fcount]));

        // shutdown once we receive our last update
        if (++_fcount == fields.length) {
            _omgr.shutdown();
        }
    }

    public void runTest ()
    {
        // request that a new TestObject be created
        _omgr.createObject(TestObject.class, this);

        // or for fun you can try this bogus create request
        // _omgr.createObject(Integer.class, sub);

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

    // the fields that will change in attribute changed events
    protected Object[] fields = {
        TestObject.FOO, TestObject.BAR, TestObject.FOO, TestObject.BAR };

    // the values we'll receive via attribute changed events
    protected Object[] values = {
        new Integer(99), "hoopie", new Integer(25), "howdy" };

    protected static PresentsDObjectMgr _omgr = new PresentsDObjectMgr();
}
