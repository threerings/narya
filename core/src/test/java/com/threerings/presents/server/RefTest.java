//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import org.junit.Test;

import com.threerings.presents.data.TestObject;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.EventListener;
import com.threerings.presents.dobj.ObjectAddedEvent;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.dobj.ObjectRemovedEvent;

import static org.junit.Assert.assertTrue;

/**
 * Tests the oid list reference tracking code.
 */
public class RefTest extends PresentsTestBase
{
    @Test public void runTest ()
    {
        // create two test objects
        _objone = _omgr.registerObject(new TestObject());
        _objone.addListener(_listener);
        _objtwo = _omgr.registerObject(new TestObject());
        _objtwo.addListener(_listener);

        // now that we have both objects, set up the references
        _objone.addToList(_objtwo.getOid());
        _objtwo.addToList(_objone.getOid());

        // and run the object manager
        _omgr.run();
    }

    protected EventListener _listener = new EventListener() {
        public void eventReceived (DEvent event) {
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
                    _omgr.harshShutdown();
                }

            } else if (event instanceof ObjectRemovedEvent) {
                // Log.info("List should be empty: " + _objone.list);
                assertTrue("List empty", _objone.list.size() == 0);
                // finally destroy the other object to complete the circle
                _objone.destroy();
            }
        }
    };

    protected TestObject _objone;
    protected TestObject _objtwo;

    protected PresentsDObjectMgr _omgr = getInstance(PresentsDObjectMgr.class);
}
