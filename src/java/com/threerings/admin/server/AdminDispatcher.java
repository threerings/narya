//
// $Id: AdminDispatcher.java,v 1.3 2004/02/25 14:39:14 mdb Exp $

package com.threerings.admin.server;

import com.threerings.admin.client.AdminService.ConfigInfoListener;
import com.threerings.admin.data.AdminMarshaller;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link AdminProvider}.
 *
 * <p> Generated from <code>
 * $Id: AdminDispatcher.java,v 1.3 2004/02/25 14:39:14 mdb Exp $
 * </code>
 */
public class AdminDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public AdminDispatcher (AdminProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new AdminMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case AdminMarshaller.GET_CONFIG_INFO:
            ((AdminProvider)provider).getConfigInfo(
                source,
                (ConfigInfoListener)args[0]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
