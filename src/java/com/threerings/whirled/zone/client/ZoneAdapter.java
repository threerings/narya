//
// $Id: ZoneAdapter.java,v 1.2 2004/08/27 02:20:50 mdb Exp $
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
 * The zone adapter makes life easier for a class that really only cares
 * about one or two of the zone observer callbacks and doesn't want to
 * provide empty implementations of the others. One can either extend zone
 * adapter, or create an anonymous instance that overrides the desired
 * callback(s).
 *
 * @see ZoneObserver
 */
public class ZoneAdapter implements ZoneObserver
{
    // documentation inherited from interface
    public void zoneWillChange (int zoneId)
    {
    }

    // documentation inherited from interface
    public void zoneDidChange (ZoneSummary summary)
    {
    }

    // documentation inherited from interface
    public void zoneChangeFailed (String reason)
    {
    }
}
