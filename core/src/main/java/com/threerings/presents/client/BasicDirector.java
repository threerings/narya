//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import com.threerings.presents.util.PresentsContext;

/**
 * Handles functionality common to nearly all client directors. They generally need to be session
 * observers so that they can set themselves up when the client logs on (by overriding {@link
 * #clientDidLogon}) and clean up after themselves when the client logs off (by overriding {@link
 * #clientDidLogoff}).
 */
public class BasicDirector
    implements SessionObserver
{
    /**
     * Derived directors will need to provide the basic director with a context that it can use to
     * register itself with the necessary entities.
     */
    protected BasicDirector (PresentsContext ctx)
    {
        // save context
        _ctx = ctx;

        // listen for session start and end
        Client client = ctx.getClient();
        client.addClientObserver(this);

        // if we're already logged on, fire off a call to fetch services
        if (client.isLoggedOn()) {
            if (isAvailable()) {
                // this is a sanity check: it will fail if this post-logon initialized director
                // claims to need service groups (it must make that known prior to logon)
                registerServices(client);
                fetchServices(client);
            }
            clientObjectUpdated(client);
        }
    }

    // documentation inherited from interface
    public void clientWillLogon (Client client)
    {
        registerServices(client);
    }

    // documentation inherited from interface
    public void clientDidLogon (Client client)
    {
        if (isAvailable()) {
            fetchServices(client);
        }
        clientObjectUpdated(client);
    }

    // documentation inherited from interface
    public void clientObjectDidChange (Client client)
    {
        clientObjectUpdated(client);
    }

    // documentation inherited from interface
    public void clientDidLogoff (Client client)
    {
    }

    /**
     * Sets whether or not this director is available in standalone mode.
     */
    public void setAvailableInStandalone (boolean available)
    {
        _availableInStandalone = available;
    }

    /**
     * Checks whether or not this director is available in standalone mode (defaults to false).
     */
    public boolean isAvailableInStandalone ()
    {
        return _availableInStandalone;
    }

    /**
     * Checks whether this director is available in the current mode.
     */
    protected boolean isAvailable ()
    {
        return isAvailableInStandalone() || !_ctx.getClient().isStandalone();
    }

    /**
     * If this director is not currently available, throws a {@link RuntimeException}.
     */
    protected void assertAvailable ()
    {
        if (!isAvailable()) {
            throw new RuntimeException(getClass().getName() +
                " not available in standalone mode!");
        }
    }

    /**
     * Called in three circumstances: when a director is created and we've already logged on; when
     * we first log on and when the client object changes after we've already logged on.
     */
    protected void clientObjectUpdated (Client client)
    {
    }

    /**
     * If a director makes use of bootstrap invocation services which are part of a bootstrap
     * service group, it should register interest in that group here with a call to {@link
     * Client#addServiceGroup}.
     */
    protected void registerServices (Client client)
    {
    }

    /**
     * Derived directors can override this method and obtain any services they'll need during their
     * operation via calls to {@link Client#getService}. If the director is available, it will
     * automatically be called when the client logs on or when the director is constructed if it is
     * constructed after the client is already logged on.
     */
    protected void fetchServices (Client client)
    {
    }

    /** The application context. */
    protected PresentsContext _ctx;

    /** Whether or not this director is available in standalone mode. */
    protected boolean _availableInStandalone = true;
}
