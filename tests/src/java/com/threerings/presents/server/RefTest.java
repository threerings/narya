//
// $Id: RefTest.java,v 1.5 2001/11/08 02:07:36 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;

/**
 * Tests the oid list reference tracking code.
 */
public class RefTest
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
        // add ourselves as an event listener to our subscribed object
        object.addListener(this);

        // keep references to our test objects
        if (_objone == null) {
            _objone = (TestObject)object;

        } else {
            _objtwo = (TestObject)object;

            // now that we have both objects, set up the references
            _objone.addToList(_objtwo.getOid());
            _objtwo.addToList(_objone.getOid());
        }
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        Log.warning("Ack. Unable to create object [cause=" + cause + "].");
    }

    public void eventReceived (DEvent event)
    {
        // Log.info("Got event: " + event);
        int toid = event.getTargetOid();

        // once we receive the second object added we can destroy the
        // target object to see if the reference is cleaned up
        if (event instanceof ObjectAddedEvent &&
            toid == _objtwo.getOid()) {
            Log.info("Destroying object two " + _objtwo + ".");
            _objtwo.destroy();

        } else if (event instanceof ObjectDestroyedEvent) {
            if (toid == _objtwo.getOid()) {
                Log.info("List won't yet be empty: " + _objone.list);
            } else {
                Log.info("Other object destroyed.");
                // go bye bye
                PresentsServer.shutdown();
            }

        } else if (event instanceof ObjectRemovedEvent) {
            Log.info("List should be empty: " + _objone.list);
            // finally destroy the other object to complete the circle
            _objone.destroy();
        }
    }

    protected TestObject _objone;
    protected TestObject _objtwo;
}
