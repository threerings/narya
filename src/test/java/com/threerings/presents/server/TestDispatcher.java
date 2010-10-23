//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.TestService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.TestMarshaller;
import java.util.List;

/**
 * Dispatches requests to the {@link TestProvider}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from TestService.java.")
public class TestDispatcher extends InvocationDispatcher<TestMarshaller>
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public TestDispatcher (TestProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public TestMarshaller createMarshaller ()
    {
        return new TestMarshaller();
    }

    @Override
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case TestMarshaller.GET_TEST_OID:
            ((TestProvider)provider).getTestOid(
                source, (TestService.TestOidListener)args[0]
            );
            return;

        case TestMarshaller.GIVE_ME_THE_POWER:
            ((TestProvider)provider).giveMeThePower(
                source, (InvocationService.ConfirmListener)args[0]
            );
            return;

        case TestMarshaller.TEST:
            ((TestProvider)provider).test(
                source, (String)args[0], ((Integer)args[1]).intValue(), this.<List<Integer>>cast(args[2]), (TestService.TestFuncListener)args[3]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
            return;
        }
    }
}
