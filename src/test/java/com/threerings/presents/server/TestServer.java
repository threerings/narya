//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import com.google.inject.Injector;

import com.threerings.presents.data.TestMarshaller;
import com.threerings.presents.data.TestObject;

public class TestServer extends PresentsServer
{
    public static TestObject testobj;

    @Override
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // register our test provider
        _invmgr.registerProvider(injector.getInstance(TestManager.class),
                                 TestMarshaller.class, "test");

        // create a test object
        testobj = _omgr.registerObject(new TestObject());
        testobj.longs.add(System.currentTimeMillis());
        long value = Integer.MAX_VALUE;
        value++;
        testobj.longs.add(value);
    }

    public static void main (String[] args)
    {
        runServer(new PresentsModule(), new PresentsServerModule(TestServer.class));
    }
}
