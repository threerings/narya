//
// $Id: DOMTest.java,v 1.1 2001/06/09 23:39:42 mdb Exp $

package com.threerings.cocktail.cher.server.test;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.cher.server.*;

/**
 * A simple test case for the dobjmgr.
 */
public class DOMTest implements Subscriber
{
    public void objectAvailable (DObject object)
    {
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

    public boolean handleEvent (DEvent event, DObject target)
    {
        Log.info("Got event [event=" + event + ", target=" + target + "].");

        // if this is the second event, request a shutdown
        AttributeChangedEvent ace = (AttributeChangedEvent)event;
        if (ace.getName().equals(TestObject.BAR)) {
            omgr.shutdown();
        }

        return true;
    }

    public static CherDObjectMgr omgr = new CherDObjectMgr();

    public static void main (String[] args)
    {
        // create our subscriber who will do things
        DOMTest sub = new DOMTest();

        // request that a new TestObject be created
        omgr.createObject(TestObject.class, sub, true);

        // or for fun you can try this bogus create request
        // omgr.createObject(Integer.class, sub, true);

        // and run the object manager
        omgr.run();
    }
}
