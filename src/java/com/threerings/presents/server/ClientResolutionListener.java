//
// $Id: ClientResolutionListener.java,v 1.3 2004/03/06 11:29:19 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.util.Name;

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
    public void clientResolved (Name username, ClientObject clobj);

    /**
     * Called when resolution fails.
     */
    public void resolutionFailed (Name username, Exception reason);
}
