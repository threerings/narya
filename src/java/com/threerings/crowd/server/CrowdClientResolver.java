//
// $Id: CrowdClientResolver.java,v 1.2 2002/03/05 05:45:53 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.ClientResolver;

import com.threerings.crowd.data.BodyObject;

/**
 * Used to configure crowd-specific client object data.
 */
public class CrowdClientResolver extends ClientResolver
{
    // documentation inherited
    public Class getClientObjectClass ()
    {
        return BodyObject.class;
    }

    // documentation inherited
    protected void resolveClientData (ClientObject clobj)
        throws Exception
    {
        super.resolveClientData(clobj);

        // just fill in the username
        ((BodyObject)clobj).username = _username;
    }
}
