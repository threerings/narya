//
// $Id: DOMTest.java,v 1.6 2001/11/08 05:40:07 mdb Exp $

package com.threerings.presents.server;

import junit.framework.Test;
import junit.framework.TestCase;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;

/**
 * A simple test case for the dobjmgr.
 */
public class DOMTest
    extends TestCase
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

        // set some values
        TestObject to = (TestObject)object;
        to.setFoo(25);
        to.setBar("howdy");
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        fail("Request failed: " + cause);
        omgr.shutdown();
    }

    public void attributeChanged (AttributeChangedEvent event)
    {
        // if this is the second event, request a shutdown
        if (event.getName().equals(TestObject.FOO)) {
            assert("foo=25", event.getIntValue() == 25);

        } else if (event.getName().equals(TestObject.BAR)) {
            assert("bar=howdy", "howdy".equals(event.getValue()));
            omgr.shutdown();
        }
    }

    public void runTest ()
    {
        // request that a new TestObject be created
        omgr.createObject(TestObject.class, this);

        // or for fun you can try this bogus create request
        // omgr.createObject(Integer.class, sub);

        // and run the object manager
        omgr.run();
    }

    public static Test suite ()
    {
        return new RefTest();
    }

    public static PresentsDObjectMgr omgr = new PresentsDObjectMgr();
}
