//
// $Id: DOMTest.java,v 1.4 2001/10/24 00:36:40 mdb Exp $

package com.threerings.presents.server.test;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;
import com.threerings.presents.server.*;

/**
 * A simple test case for the dobjmgr.
 */
public class DOMTest implements Subscriber, AttributeChangeListener
{
    public void objectAvailable (DObject object)
    {
        // add ourselves as a listener
        object.addListener(this);

        Log.info("Object available: " + object);
        // set some values
        TestObject to = (TestObject)object;
        to.setFoo(25);
        to.setBar("howdy");
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        Log.info("Request failed: " + cause);
        omgr.shutdown();
    }

    public void attributeChanged (AttributeChangedEvent event)
    {
        Log.info("Got event [event=" + event + "].");

        // if this is the second event, request a shutdown
        if (event.getName().equals(TestObject.BAR)) {
            omgr.shutdown();
        }
    }

    public static PresentsDObjectMgr omgr = new PresentsDObjectMgr();

    public static void main (String[] args)
    {
        // create our subscriber who will do things
        DOMTest sub = new DOMTest();

        // request that a new TestObject be created
        omgr.createObject(TestObject.class, sub);

        // or for fun you can try this bogus create request
        // omgr.createObject(Integer.class, sub);

        // and run the object manager
        omgr.run();
    }
}
