//
// $Id: CrowdClient.java,v 1.3 2001/10/11 04:07:51 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.server.PresentsClient;
import com.threerings.crowd.data.BodyObject;

/**
 * The crowd client extends the presents client and does some
 * initializations necessary for the crowd services.
 */
public class CrowdClient extends PresentsClient
{
    protected void sessionWillStart ()
    {
        super.sessionWillStart();

        // cast our client object to a body object
        _bodobj = (BodyObject)_clobj;

        // and configure our username
        _bodobj.setUsername(_username);

        // register our body object mapping
        CrowdServer.mapBody(_username, _bodobj);
    }

    protected void sessionWillResume ()
    {
        super.sessionWillResume();

        // nothing to do here presently
    }

    protected void sessionDidTerminate ()
    {
        super.sessionDidTerminate();

        // unregister our body object mapping
        CrowdServer.unmapBody(_username);
    }

    protected BodyObject _bodobj;
}
