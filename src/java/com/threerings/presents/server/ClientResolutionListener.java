//
// $Id: ClientResolutionListener.java,v 1.2 2002/11/29 23:40:01 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.data.ClientObject;

/**
 * Entites that wish to resolve client objects must implement this
 * interface so as to partake in the asynchronous process of client
 * object resolution.
 */
public interface ClientResolutionListener
{
    /**
     * Called when resolution completed successfully.
     */
    public void clientResolved (String username, ClientObject clobj);

    /**
     * Called when resolution fails.
     */
    public void resolutionFailed (String username, Exception reason);
}
