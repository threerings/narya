//
// $Id: ClientAdapter.java,v 1.2 2001/09/13 19:08:21 mdb Exp $

package com.threerings.cocktail.cher.client;

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
    public void clientDidLogon (Client client)
    {
    }

    // documentation inherited
    public void clientFailedToLogon (Client client, Exception cause)
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
}
