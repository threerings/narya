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

package com.threerings.bureau.client {

import com.threerings.presents.client.Client;
import com.threerings.bureau.data.BureauCredentials;
import com.threerings.bureau.util.BureauContext;
import com.threerings.presents.dobj.DObjectManager;

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
    public function BureauClient (token :String, bureauId :String)
    {
        super(null);

        _bureauId = bureauId;

        var creds :BureauCredentials = new BureauCredentials(_bureauId);
        creds.sessionToken = token;
        _creds = creds;

        _ctx = createContext();
        _director = createDirector();
    }

    public function getBureauDirector () :BureauDirector
    {
        return _director;
    }

    public function getBureauId () :String
    {
        return _bureauId;
    }

    protected function createDirector () :BureauDirector
    {
        throw new Error("Abstract method");
    }

    protected function createContext () :BureauContext
    {
        return new Context(this);
    }

    protected var _ctx :BureauContext;
    protected var _bureauId :String;
    protected var _director :BureauDirector;
}

}

import com.threerings.bureau.client.BureauClient;
import com.threerings.bureau.client.BureauDirector;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.client.Client;
import com.threerings.bureau.util.BureauContext;

class Context
    implements BureauContext
{
    function Context (client :BureauClient)
    {
        _client = client;
    }

    public function getBureauDirector () :BureauDirector
    {
        return _client.getBureauDirector();
    }

    public function getDObjectManager () :DObjectManager
    {
        return _client.getDObjectManager();
    }

    public function getClient () :Client
    {
        return _client;
    }

    public function getBureauId () :String
    {
        return _client.getBureauId();
    }

    protected var _client :BureauClient;
}
