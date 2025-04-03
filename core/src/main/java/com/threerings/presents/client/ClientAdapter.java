//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

/**
 * The client adapter makes life easier for client observer classes that
 * only care about one or two of the client observer callbacks. They can
 * either extend client adapter or create an anonymous class that extends
 * it and overrides just the callbacks they care about.
 *
 * <p> Note that the client adapter defaults to always ratifying a call to
 * {@link #clientWillLogoff} by returning true.
 */
public class ClientAdapter implements ClientObserver
{
    // documentation inherited
    public void clientWillLogon (Client client)
    {
    }

    // documentation inherited
    public void clientDidLogon (Client client)
    {
    }

    // documentation inherited
    public void clientFailedToLogon (Client client, Exception cause)
    {
    }

    // documentation inherited from interface
    public void clientObjectDidChange (Client client)
    {
    }

    // documentation inherited
    public void clientConnectionFailed (Client client, Exception cause)
    {
    }

    // documentation inherited
    public boolean clientWillLogoff (Client client)
    {
        return true;
    }

    // documentation inherited
    public void clientDidLogoff (Client client)
    {
    }

    // documentation inherited
    public void clientDidClear (Client client)
    {
    }
}
