//
// $Id: CrowdClient.java,v 1.9 2002/09/17 01:23:09 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.server.PresentsClient;

import com.threerings.crowd.data.BodyObject;

/**
 * The crowd client extends the presents client but doesn't really do
 * anything at present. It exists mainly so that implementation systems
 * will extend it and ensure that we have the option of adding
 * functionality here in the future.
 */
public class CrowdClient extends PresentsClient
{
    // documentation inherited
    public void setUsername (String username)
    {
        super.setUsername(username);

        // update the username in our body object
        ((BodyObject)_clobj).setUsername(username);
    }
}
