//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

import com.samskivert.util.Config;
import com.samskivert.util.RunQueue;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

import com.threerings.bureau.data.BureauCredentials;
import com.threerings.bureau.util.BureauContext;

/**
 * Represents a client embedded in a bureau.
 */
public abstract class BureauClient extends Client
{
    /**
     * Creates a new client.
     * @param runQueue the place to post tasks required by clients
     */
    public BureauClient (String bureauId, String sharedSecret, RunQueue runQueue)
    {
        super(null, runQueue);
        _bureauId = bureauId;
        _creds = new BureauCredentials(_bureauId, sharedSecret);
        _ctx = createContext();
        _director = createDirector();
    }

    protected abstract BureauDirector createDirector ();

    protected BureauContext createContext ()
    {
        return new BureauContext() {
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
