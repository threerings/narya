//
// $Id: CommunicationAuthorizer.java,v 1.1 2003/07/18 01:54:51 eric Exp $

package com.threerings.crowd.chat.server;

import com.threerings.presents.data.ClientObject;

/**
 * An interface used to check and see if the specified client is allowed
 * to make said communication.
 */
public interface CommunicationAuthorizer
{
    public boolean authorized (ClientObject caller);
}
