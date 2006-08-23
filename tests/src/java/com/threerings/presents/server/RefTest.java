//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.server;

import junit.framework.Test;
import junit.framework.TestCase;

import com.threerings.presents.data.TestObject;
import com.threerings.presents.dobj.*;

/**
 * Tests the oid list reference tracking code.
 */
public class RefTest extends TestCase
    implements EventListener
{
    public static Test suite ()
    {
        return new RefTest();
    }

    public RefTest ()
    {
        super(RefTest.class.getName());
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
                _omgr.harshShutdown();
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
        _objone = _omgr.registerObject(new TestObject());
        _objone.addListener(this);
        _objtwo = _omgr.registerObject(new TestObject());
        _objtwo.addListener(this);

        // now that we have both objects, set up the references
        _objone.addToList(_objtwo.getOid());
        _objtwo.addToList(_objone.getOid());

        // and run the object manager
        _omgr.run();
    }

    protected TestObject _objone;
    protected TestObject _objtwo;

    protected static PresentsDObjectMgr _omgr = new PresentsDObjectMgr();
}
