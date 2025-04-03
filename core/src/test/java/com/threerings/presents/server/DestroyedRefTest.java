//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import org.junit.Test;

import com.threerings.presents.data.TestObject;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.EventListener;
import com.threerings.presents.dobj.ObjectAddedEvent;
import com.threerings.presents.dobj.ObjectDestroyedEvent;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static com.threerings.presents.Log.log;

/**
 * Tests that the dobjmgr will not allow a destroyed object to be added to an oid list.
 */
public class DestroyedRefTest extends PresentsTestBase
{
    @Test public void runTest ()
    {
        // create two test objects
        _objone = _omgr.registerObject(new TestObject());
        _objone.addListener(_listener);
        _objtwo = _omgr.registerObject(new TestObject());
        _objtwo.addListener(_listener);

        // add object one to object two twice in a row to make sure repeated
        // adds don't result in the object being listed twice
        _objtwo.addToList(_objone.getOid());
        log.info("The following addToList() should be ignored.");
        _objtwo.addToList(_objone.getOid());

        // now that we have both objects, try to set up the reference.  first we queue up a destroy
        // event for object two, then we try to reference it on object one's oid list
        _objtwo.destroy();
        _objone.addToList(_objtwo.getOid());

        // finally dispatch an event on which we can trigger our exit
        _objone.setFoo(1);

        // and run the object manager
        _omgr.run();
    }

    protected EventListener _listener = new EventListener() {
        public void eventReceived (DEvent event) {
            int toid = event.getTargetOid();

            // when we get the attribute change, we can exit
            if (event instanceof ObjectDestroyedEvent) {
                log.info("The upcoming object added event should be rejected.");

            } else if (event instanceof ObjectAddedEvent &&
                       toid == _objtwo.getOid()) {
                assertTrue("list should contain only one oid",
                           _objtwo.list.size() == 1);

            } else if (event instanceof AttributeChangedEvent) {
                // go bye bye
                _omgr.harshShutdown();

            } else {
                fail("Got unexpected event: " + event);
            }
        }
    };

    protected TestObject _objone, _objtwo;

    protected PresentsDObjectMgr _omgr = getInstance(PresentsDObjectMgr.class);
}
