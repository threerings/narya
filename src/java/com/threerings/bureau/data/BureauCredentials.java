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

import com.threerings.util.Name;

import com.threerings.presents.net.Credentials;

/**
 * Extends the basic credentials to provide bureau-specific fields.
 */
public class BureauCredentials extends Credentials
{
    public static final String PREFIX = "@@bureau:";
    public static final String SUFFIX = "@@";

    /**
     * The token to pass to the server when logging in. This is usually just passed to the bureau
     * on the command line to guard against outside connections being established.
     */
    public String sessionToken;

    /**
     * The id of the bureau logging in.
     */
    public String bureauId;

    /**
     * Test if a given name object matches the name that we generate.
     */
    public static boolean isBureau (Name name)
    {
        String normal = name.getNormal();
        return normal.startsWith(PREFIX) && normal.endsWith(SUFFIX);
    }

    /**
     * Extract the buerauId from the name that we generate.
     */
    public static String extractBureauId (Name name)
    {
        String normal = name.getNormal();
        int prefixPos = normal.indexOf(PREFIX);
        int suffixPos = normal.lastIndexOf(SUFFIX);
        if (prefixPos != 0 || suffixPos != normal.length() - SUFFIX.length()) {
            return null;
        }

        return normal.substring(prefixPos + PREFIX.length(), suffixPos);
    }

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
        this.bureauId = bureauId;
    }

    @Override // inherit documentation
    protected void toString (StringBuilder buf)
    {
        super.toString(buf);
        buf.append(" id=").append(bureauId).
            append(" token=").append(sessionToken);
    }
}
