//
// $Id$
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

package com.threerings.parlor.data;

import com.threerings.presents.data.InvocationCodes;

/**
 * Contains codes used by the parlor invocation services.
 */
public interface ParlorCodes extends InvocationCodes
{
    /** The response code for an accepted invitation. */
    public static final int INVITATION_ACCEPTED = 0;

    /** The response code for a refused invitation. */
    public static final int INVITATION_REFUSED = 1;

    /** The response code for a countered invitation. */
    public static final int INVITATION_COUNTERED = 2;

    /** An error code explaining that an invitation was rejected because
     * the invited user was not online at the time the invitation was
     * received. */
    public static final String INVITEE_NOT_ONLINE = "m.invitee_not_online";

    /** An error code returned when a user requests to join a table that
     * doesn't exist. */
    public static final String NO_SUCH_TABLE = "m.no_such_table";

    /** An error code returned when a user requests to join a table at a
     * position that is not valid. */
    public static final String INVALID_TABLE_POSITION =
        "m.invalid_table_position";

    /** An error code returned when a user requests to join a table in a
     * position that is already occupied. */
    public static final String TABLE_POSITION_OCCUPIED =
        "m.table_position_occupied";

    /** An error code returned when a user requests to leave a table that
     * they were not sitting at in the first place. */
    public static final String NOT_AT_TABLE = "m.not_at_table";
}
