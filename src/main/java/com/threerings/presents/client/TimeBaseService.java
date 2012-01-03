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

package com.threerings.presents.client;

import com.threerings.presents.data.ClientObject;

/**
 * Provides a means by which to obtain access to a time base object which can be used to convert
 * delta times into absolute times.
 */
public interface TimeBaseService extends InvocationService<ClientObject>
{
    /**
     * Used to communicated the result of a {@link TimeBaseService#getTimeOid} request.
     */
    public static interface GotTimeBaseListener extends InvocationListener
    {
        /**
         * Communicates the result of a successful {@link TimeBaseService#getTimeOid} request.
         */
        void gotTimeOid (int timeOid);
    }

    /**
     * Requests the oid of the specified time base object be fetched.
     */
    void getTimeOid (String timeBase, GotTimeBaseListener listener);
}
