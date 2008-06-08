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

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.threerings.presents.data.TestObject;
import com.threerings.presents.dobj.*;

import static com.threerings.presents.Log.log;

public class TestServer extends PresentsServer
{
    public static TestObject testobj;

    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // register our test provider
        _invmgr.registerDispatcher(new TestDispatcher(new TestManager()), "test");

        // create a test object
        testobj = _omgr.registerObject(new TestObject());
        testobj.longs.add(System.currentTimeMillis());
        long value = Integer.MAX_VALUE;
        value++;
        testobj.longs.add(value);
    }

    public static void main (String[] args)
    {
        Injector injector = Guice.createInjector(new Module());
        TestServer server = injector.getInstance(TestServer.class);
        try {
            server.init(injector);
            server.run();
        } catch (Exception e) {
            log.warning("Unable to initialize server.", e);
        }
    }
}
