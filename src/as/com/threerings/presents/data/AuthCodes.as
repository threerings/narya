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

package com.threerings.presents.data {

/**
 * Basic authentication response codes.
 */
public class AuthCodes
{
    /** A code indicating that no user exists with the specified
     * username. */
    public static const NO_SUCH_USER :String = "m.no_such_user";

    /** A code indicating that the supplied password was invalid. */
    public static const INVALID_PASSWORD :String = "m.invalid_password";

    /** A code indicating that an internal server error occurred while
     * trying to log the user on. */
    public static const SERVER_ERROR :String = "m.server_error";

    /** A code indicating that the server is not available at the moment. */
    public static const SERVER_UNAVAILABLE :String = "m.server_unavailable";

    /** A code indicating that we failed to connect to the server on a port and
     * are trying the next port in the list. */
    public static const TRYING_NEXT_PORT :String = "m.trying_next_port";
}
}
