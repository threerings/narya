//
// $Id: AuthCodes.java,v 1.3 2004/08/27 02:20:19 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.data;

/**
 * Basic authentication response codes.
 */
public interface AuthCodes
{
    /** A code indicating that no user exists with the specified
     * username. */
    public static final String NO_SUCH_USER = "m.no_such_user";

    /** A code indicating that the supplied password was invalid. */
    public static final String INVALID_PASSWORD = "m.invalid_password";

    /** A code indicating that an internal server error occurred while
     * trying to log the user on. */
    public static final String SERVER_ERROR = "m.server_error";

    /** A code indicating that the server is not available at the moment. */
    public static final String SERVER_UNAVAILABLE = "m.server_unavailable";
}
