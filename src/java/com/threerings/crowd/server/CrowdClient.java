//
// $Id: CrowdClient.java,v 1.2 2001/08/04 01:55:41 mdb Exp $

package com.threerings.cocktail.party.server;

import com.threerings.cocktail.cher.server.CherClient;
import com.threerings.cocktail.party.data.BodyObject;

/**
 * The party client extends the cher client and does some initializations
 * necessary for the party services.
 */
public class PartyClient extends CherClient
{
    protected void sessionWillStart ()
    {
        super.sessionWillStart();

        // cast our client object to a body object
        _bodobj = (BodyObject)_clobj;

        // and configure our username
        _bodobj.setUsername(_username);

        // register our body object mapping
        PartyServer.mapBody(_username, _bodobj);
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
        PartyServer.unmapBody(_username);
    }

    protected BodyObject _bodobj;
}
