//
// $Id: BasicDirector.java,v 1.2 2002/09/20 00:54:39 mdb Exp $

package com.threerings.presents.client;

import com.threerings.presents.util.PresentsContext;

/**
 * Handles functionality common to nearly all client directors. They
 * generally need to be session observers so that they can set themselves
 * up when the client logs on (by overriding {@link #clientDidLogon}) and
 * clean up after themselves when the client logs off (by overriding
 * {@link #clientDidLogoff}).
 */
public class BasicDirector
    implements SessionObserver
{
    /**
     * Derived directors will need to provide the basic director with a
     * context that it can use to register itself with the necessary
     * entities.
     */
    protected BasicDirector (PresentsContext ctx)
    {
        // listen for session start and end
        Client client = ctx.getClient();
        client.addClientObserver(this);

        // if we're already logged on, fire off a call to fetch services
        if (client.isLoggedOn()) {
            fetchServices(client);
        }
    }

    // documentation inherited from interface
    public void clientDidLogon (Client client)
    {
        fetchServices(client);
    }

    // documentation inherited from interface
    public void clientObjectDidChange (Client client)
    {
    }

    // documentation inherited from interface
    public void clientDidLogoff (Client client)
    {
    }

    /**
     * Derived directors can override this method and obtain any services
     * they'll need during their operation via calls to {@link
     * Client#getService}. It will automatically be called when the client
     * logs on or when the director is constructed if it is constructed
     * after the client is already logged on.
     */
    protected void fetchServices (Client client)
    {
    }
}
