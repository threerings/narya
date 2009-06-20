//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.crowd.client {

import com.threerings.presents.client.Client;
import com.threerings.presents.net.Credentials;

import com.threerings.crowd.data.BodyMarshaller;
import com.threerings.crowd.data.CrowdPermissionPolicy;

/**
 * Users of the Crowd services should extend this client so that it can ensure that certain
 * ActionScript classes that will arrive over the wire as a result of using basic Crowd services
 * will be included in the client.
 */
public class CrowdClient extends Client
{
    // statically reference classes we require
    BodyMarshaller;
    CrowdPermissionPolicy;

    public function CrowdClient (creds :Credentials)
    {
        super(creds);
    }
}
}
