//
// $Id: ParlorDispatcher.java,v 1.1 2002/08/14 19:07:54 mdb Exp $

package com.threerings.parlor.server;

import com.threerings.parlor.client.ParlorService;
import com.threerings.parlor.client.ParlorService.InviteListener;
import com.threerings.parlor.client.ParlorService.TableListener;
import com.threerings.parlor.data.ParlorMarshaller;
import com.threerings.parlor.game.GameConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.InvocationListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link ParlorProvider}.
 */
public class ParlorDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public ParlorDispatcher (ParlorProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new ParlorMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case ParlorMarshaller.INVITE:
            ((ParlorProvider)provider).invite(
                source,
                (String)args[0], (GameConfig)args[1], (InviteListener)args[2]
            );
            return;

        case ParlorMarshaller.RESPOND:
            ((ParlorProvider)provider).respond(
                source,
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), (Object)args[2], (InvocationListener)args[3]
            );
            return;

        case ParlorMarshaller.CANCEL:
            ((ParlorProvider)provider).cancel(
                source,
                ((Integer)args[0]).intValue(), (InvocationListener)args[1]
            );
            return;

        case ParlorMarshaller.CREATE_TABLE:
            ((ParlorProvider)provider).createTable(
                source,
                ((Integer)args[0]).intValue(), (GameConfig)args[1], (TableListener)args[2]
            );
            return;

        case ParlorMarshaller.JOIN_TABLE:
            ((ParlorProvider)provider).joinTable(
                source,
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), ((Integer)args[2]).intValue(), (InvocationListener)args[3]
            );
            return;

        case ParlorMarshaller.LEAVE_TABLE:
            ((ParlorProvider)provider).leaveTable(
                source,
                ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), (InvocationListener)args[2]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
