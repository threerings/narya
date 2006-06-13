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

package com.threerings.whirled.zone.client;

import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * The zone observer interface makes it possible for entities to be
 * notified when the client moves to a new zone.
 */
public interface ZoneObserver
{
    /**
     * Called when we begin the process of switching to a new zone. This
     * will be followed by a call to {@link #zoneDidChange} to indicate
     * that the change was successful or {@link #zoneChangeFailed} if the
     * change fails.
     *
     * @param zoneId the zone id of the zone to which we are changing.
     */
    public void zoneWillChange (int zoneId);

    /**
     * Called when we have switched to a new zone.
     *
     * @param summary the summary information for the new zone or null if
     * we have switched to no zone.
     */
    public void zoneDidChange (ZoneSummary summary);

    /**
     * This is called on all zone observers when a zone change request is
     * rejected by the server or fails for some other reason.
     *
     * @param reason the reason code that explains why the zone change
     * request was rejected or otherwise failed.
     */
    public void zoneChangeFailed (String reason);
}
