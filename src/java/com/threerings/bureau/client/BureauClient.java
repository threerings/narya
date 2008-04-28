//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.bureau.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.net.Credentials;
import com.samskivert.util.RunQueue;
import com.threerings.bureau.data.BureauCredentials;
import com.threerings.bureau.util.BureauContext;
import com.threerings.presents.dobj.DObjectManager;
import com.samskivert.util.Config;

/** 
 * Represents a client embedded in a bureau.
 */
public class BureauClient extends Client
{
    /**
     * Creates a new client.
     * @param creds the credentials supplied during connection
     * @param runQueue the place to post tasks required by clients
     */
    public BureauClient (String token, String bureauId, RunQueue runQueue)
    {
        super(null, runQueue);

        _bureauId = bureauId;

        BureauCredentials creds = new BureauCredentials();
        creds.sessionToken = token;
        _creds = creds;

        _ctx = createContext();
        _director = new BureauDirector(_ctx);
    }

    protected BureauContext createContext ()
    {
        return new BureauContext () {
            public BureauDirector getBureauDirector () {
                return _director;
            }
            public DObjectManager getDObjectManager () {
                return _omgr;
            }
            public Client getClient () {
                return BureauClient.this;
            }
            public Config getConfig () {
                return _config;
            }
            public String getBureauId () {
                return _bureauId;
            }
        };
    }

    protected BureauContext _ctx;
    protected String _bureauId;
    protected BureauDirector _director;
    protected Config _config = new Config("bureau");
}
