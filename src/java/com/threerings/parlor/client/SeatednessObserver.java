//
// $Id: SeatednessObserver.java,v 1.3 2004/08/27 02:20:12 mdb Exp $
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

package com.threerings.parlor.client;

/**
 * Entites that wish to hear about when we sit down at a table or stand up
 * from a table can implement this interface and register themselves with
 * the {@link TableDirector}.
 */
public interface SeatednessObserver
{
    /**
     * Called when this client sits down at or stands up from a table.
     *
     * @param isSeated true if the client is now seated at a table, false
     * if they are now no longer seated at a table.
     */
    public void seatednessDidChange (boolean isSeated);
}
