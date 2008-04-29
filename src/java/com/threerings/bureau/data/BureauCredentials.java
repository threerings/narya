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

package com.threerings.bureau.data;

import com.threerings.presents.net.Credentials;
import com.threerings.util.Name;

/**
 * Extends the basic credentials to provide bureau-specific fields.
 */
public class BureauCredentials extends Credentials
{
    /**
     * The token to pass to the server when logging in. This is usually just passed to the bureau 
     * on the command line to guard against outside connections being established.
     */
    public String sessionToken;

    /**
     * Creates an empty credentials for streaming. Should not be used directly.
     */
    public BureauCredentials ()
    {
    }

    /**
     * Creates new credentials for a specific bureau.
     */
    public BureauCredentials (String bureauId)
    {
        super(new Name("@@bureau:" + bureauId + "@@"));
    }

    // inherit documentation - from Object
    public String toString ()
    {
        return super.toString() + ", token=" + sessionToken;
    }
}
