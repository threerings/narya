//
// $Id: DestroyedRefTest.java,v 1.4 2001/10/24 00:36:40 mdb Exp $

package com.threerings.presents.server.test;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;
import com.threerings.presents.server.PresentsServer;

/**
 * Tests that the dobjmgr will not allow a destroyed object to be added to
 * an oid list.
 */
public class DestroyedRefTest
    implements Runnable, Subscriber, EventListener
{
    public void run ()
    {
        // create two test objects
        PresentsServer.omgr.createObject(TestObject.class, this);
        PresentsServer.omgr.createObject(TestObject.class, this);
    }

    public void objectAvailable (DObject object)
    {
        // add ourselves as an event listener
        object.addListener(this);

        // keep references to our test objects
        if (_objone == null) {
            _objone = (TestObject)object;

        } else {
            _objtwo = (TestObject)object;

            // add object one to object two twice in a row to make sure
            // repeated adds don't result in the object being listed twice
            _objtwo.addToList(_objone.getOid());
            _objtwo.addToList(_objone.getOid());

            // now that we have both objects, try to set up the reference.
            // first we queue up a destroy event for object two, then we
            // try to reference it on object one's oid list
            _objtwo.destroy();
            _objone.addToList(_objtwo.getOid());

            // finally dispatch an event on which we can trigger our exit
            _objone.setFoo(1);
        }
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        Log.warning("Ack. Unable to create object [cause=" + cause + "].");
    }

    public void eventReceived (DEvent event)
    {
        int toid = event.getTargetOid();

        // when we get the attribute change, we can exit
        if (event instanceof ObjectDestroyedEvent) {
            Log.info("The upcoming object added event should be rejected.");

        } else if (event instanceof ObjectAddedEvent &&
                   toid == _objtwo.getOid()) {
            Log.info("list should contain only one oid: " + _objtwo.list);

        } else if (event instanceof AttributeChangedEvent) {
            // go bye bye
            PresentsServer.shutdown();

        } else {
            Log.info("Got unexpected event: " + event);
        }
    }

    protected TestObject _objone;
    protected TestObject _objtwo;
}
