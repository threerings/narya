//
// $Id: RefTest.java,v 1.2 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.server.test;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;
import com.threerings.presents.server.PresentsServer;

/**
 * Tests the oid list reference tracking code.
 */
public class RefTest
    implements Runnable, Subscriber
{
    public void run ()
    {
        // create two test objects
        PresentsServer.omgr.createObject(TestObject.class, this, true);
        PresentsServer.omgr.createObject(TestObject.class, this, true);
    }

    public void objectAvailable (DObject object)
    {
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

    public boolean handleEvent (DEvent event, DObject target)
    {
        // Log.info("Got event: " + event);

        // once we receive the second object added we can destroy the
        // target object to see if the reference is cleaned up
        if (event instanceof ObjectAddedEvent && target == _objtwo) {
            Log.info("Destroying object two " + _objtwo + ".");
            _objtwo.destroy();

        } else if (event instanceof ObjectDestroyedEvent) {
            if (target == _objtwo) {
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

        return true;
    }

    protected TestObject _objone;
    protected TestObject _objtwo;
}
