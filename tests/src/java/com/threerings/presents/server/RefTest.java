//
// $Id: RefTest.java,v 1.8 2002/04/15 16:34:36 shaper Exp $

package com.threerings.presents.server;

import junit.framework.Test;
import junit.framework.TestCase;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;

/**
 * Tests the oid list reference tracking code.
 */
public class RefTest
    extends TestCase
    implements Subscriber, EventListener
{
    public RefTest ()
    {
        super(RefTest.class.getName());
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
        fail("Ack. Unable to create object [cause=" + cause + "].");
    }

    public void eventReceived (DEvent event)
    {
        // Log.info("Got event: " + event);
        int toid = event.getTargetOid();

        // once we receive the second object added we can destroy the
        // target object to see if the reference is cleaned up
        if (event instanceof ObjectAddedEvent &&
            toid == _objtwo.getOid()) {
            // Log.info("Destroying object two " + _objtwo + ".");
            _objtwo.destroy();

        } else if (event instanceof ObjectDestroyedEvent) {
            if (toid == _objtwo.getOid()) {
                // Log.info("List won't yet be empty: " + _objone.list);
                assertTrue("List not empty", _objone.list.size() > 0);
            } else {
                // Log.info("Other object destroyed.");
                // go bye bye
                _omgr.shutdown();
            }

        } else if (event instanceof ObjectRemovedEvent) {
            // Log.info("List should be empty: " + _objone.list);
            assertTrue("List empty", _objone.list.size() == 0);
            // finally destroy the other object to complete the circle
            _objone.destroy();
        }
    }

    public void runTest ()
    {
        // create two test objects
        _omgr.createObject(TestObject.class, this);
        _omgr.createObject(TestObject.class, this);

        // and run the object manager
        _omgr.run();
    }

    public static Test suite ()
    {
        return new RefTest();
    }

    protected TestObject _objone;
    protected TestObject _objtwo;

    protected static PresentsDObjectMgr _omgr = new PresentsDObjectMgr();
}
