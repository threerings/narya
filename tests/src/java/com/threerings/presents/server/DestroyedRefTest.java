//
// $Id: DestroyedRefTest.java,v 1.8 2002/04/15 16:34:36 shaper Exp $

package com.threerings.presents.server;

import junit.framework.Test;
import junit.framework.TestCase;

import com.threerings.presents.Log;
import com.threerings.presents.dobj.*;

/**
 * Tests that the dobjmgr will not allow a destroyed object to be added to
 * an oid list.
 */
public class DestroyedRefTest
    extends TestCase
    implements Subscriber, EventListener
{
    public DestroyedRefTest ()
    {
        super(DestroyedRefTest.class.getName());
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
            Log.info("The following addToList() should be ignored.");
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
        fail("Ack. Unable to create object [cause=" + cause + "].");
    }

    public void eventReceived (DEvent event)
    {
        int toid = event.getTargetOid();

        // when we get the attribute change, we can exit
        if (event instanceof ObjectDestroyedEvent) {
            Log.info("The upcoming object added event should be rejected.");

        } else if (event instanceof ObjectAddedEvent &&
                   toid == _objtwo.getOid()) {
            assertTrue("list should contain only one oid",
                       _objtwo.list.size() == 1);

        } else if (event instanceof AttributeChangedEvent) {
            // go bye bye
            _omgr.shutdown();

        } else {
            fail("Got unexpected event: " + event);
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
        return new DestroyedRefTest();
    }

    protected TestObject _objone;
    protected TestObject _objtwo;

    protected static PresentsDObjectMgr _omgr = new PresentsDObjectMgr();
}
