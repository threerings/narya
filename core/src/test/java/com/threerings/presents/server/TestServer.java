//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.google.inject.Injector;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.TestClientObject;
import com.threerings.presents.data.TestMarshaller;
import com.threerings.presents.data.TestObject;
import com.threerings.presents.net.AuthRequest;
import com.threerings.util.Name;

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

        _clmgr.setDefaultSessionFactory(new SessionFactory() {
            @Override public Class<? extends PresentsSession> getSessionClass (AuthRequest areq) {
                return PresentsSession.class;
            }
            @Override public Class<? extends ClientResolver> getClientResolverClass (Name username) {
                return TestClientResolver.class;
            }
        });

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

    protected static class TestClientResolver extends ClientResolver {
        @Override public ClientObject createClientObject () {
            return new TestClientObject();
        }
    }
}
