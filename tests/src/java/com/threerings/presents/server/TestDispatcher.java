//
// $Id: TestDispatcher.java,v 1.1 2002/08/14 19:08:00 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.TestService;
import com.threerings.presents.client.TestService.TestFuncListener;
import com.threerings.presents.client.TestService.TestOidListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.TestMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link TestProvider}.
 */
public class TestDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public TestDispatcher (TestProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new TestMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case TestMarshaller.TEST:
            ((TestProvider)provider).test(
                source,
                (String)args[0], ((Integer)args[1]).intValue(), (TestFuncListener)args[2]
            );
            return;

        case TestMarshaller.GET_TEST_OID:
            ((TestProvider)provider).getTestOid(
                source,
                (TestOidListener)args[0]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
