//
// $Id: WhirledClient.java,v 1.2 2002/12/03 06:58:57 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdClient;

import com.threerings.whirled.data.ScenedBodyObject;

/**
 * The client object used by client management on the Whirled server.
 */
public class WhirledClient extends CrowdClient
{
    // documentation inherited from interface
    protected void clearLocation (BodyObject bobj)
    {
        WhirledServer.screg.sceneprov.leaveOccupiedScene(
            (ScenedBodyObject)bobj);
    }
}
