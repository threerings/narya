//
// $Id: CrowdClient.java,v 1.6 2002/02/03 03:09:06 mdb Exp $

package com.threerings.crowd.server;

import com.threerings.presents.server.PresentsClient;
import com.threerings.crowd.data.BodyObject;

/**
 * The crowd client extends the presents client and does some
 * initializations necessary for the crowd services.
 */
public class CrowdClient extends PresentsClient
{
    protected void sessionWillStart (Object authInfo)
    {
        super.sessionWillStart(authInfo);

        // cast our client object to a body object
        _bodobj = (BodyObject)_clobj;

        // and configure our username (we use the setImmediate form so
        // that entities later in the session start processing can access
        // fields set in the body object without having to wait for them
        // to be flushed through the dobject queue)
        _bodobj.setUsernameImmediate(_username);

        // register our body object mapping
        CrowdServer.mapBody(_username, _bodobj);
    }

    protected void sessionWillResume (Object authInfo)
    {
        super.sessionWillResume(authInfo);

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
