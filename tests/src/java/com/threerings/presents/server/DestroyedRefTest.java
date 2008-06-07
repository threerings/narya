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

import org.junit.Test;
import org.junit.runner.RunWith;

import atunit.AtUnit;
import atunit.Container;
import atunit.Unit;

import com.google.inject.Inject;

import com.threerings.presents.data.TestObject;
import com.threerings.presents.dobj.*;

import static org.junit.Assert.*;

import static com.threerings.presents.Log.log;

/**
 * Tests that the dobjmgr will not allow a destroyed object to be added to
 * an oid list.
 */
@RunWith(AtUnit.class)
@Container(Container.Option.GUICE)
public class DestroyedRefTest
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

        // now that we have both objects, try to set up the reference.  first
        // we queue up a destroy event for object two, then we try to reference
        // it on object one's oid list
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

    @Inject @Unit protected PresentsDObjectMgr _omgr;
}
