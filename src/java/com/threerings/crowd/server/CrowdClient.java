//
// $Id: CrowdClient.java,v 1.1 2001/08/03 02:28:20 mdb Exp $

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
        // cast our client object to a body object
        _bodobj = (BodyObject)_clobj;

        // and configure our username
        _bodobj.setUsername(_username);
    }

    protected void sessionWillResume ()
    {
    }

    protected BodyObject _bodobj;
}
