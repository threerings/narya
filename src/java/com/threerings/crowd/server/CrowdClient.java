//
// $Id: CrowdClient.java,v 1.4 2001/12/03 20:14:51 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.server.PresentsClient;

import com.threerings.crowd.data.BodyObject;

/**
 * The crowd client extends the presents client and does some
 * initializations necessary for the crowd services.
 */
public class CrowdClient extends PresentsClient
{
    protected void sessionWillStart (AuthResponseData rdata)
    {
        super.sessionWillStart(rdata);

        // cast our client object to a body object
        _bodobj = (BodyObject)_clobj;

        // and configure our username
        _bodobj.setUsername(_username);

        // register our body object mapping
        CrowdServer.mapBody(_username, _bodobj);
    }

    protected void sessionWillResume (AuthResponseData rdata)
    {
        super.sessionWillResume(rdata);

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
