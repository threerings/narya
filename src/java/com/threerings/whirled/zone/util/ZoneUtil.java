//
// $Id: ZoneUtil.java,v 1.4 2004/08/27 02:20:53 mdb Exp $
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

package com.threerings.whirled.zone.util;

/**
 * Server-specific, zone-related utility functions.
 */
public class ZoneUtil
{
    /**
     * Composes the zone type and zone id into a qualified zone id. A
     * qualified zone id is what should be passed around so that the
     * server can determine the zone type from the zone id when necessary.
     */
    public static int qualifyZoneId (byte zoneType, int zoneId)
    {
        int qualifiedZoneId = zoneType;
        qualifiedZoneId <<= 24;
        qualifiedZoneId |= zoneId;
        return qualifiedZoneId;
    }

    /**
     * Extracts the zone type from a qualified zone id.
     */
    public static int zoneType (int qualifiedZoneId)
    {
        return (0xFF000000 & qualifiedZoneId) >> 24;
    }

    /**
     * Extracts the zone id from a qualified zone id.
     */
    public static int zoneId (int qualifiedZoneId)
    {
        return (0x00FFFFFF & qualifiedZoneId);
    }

    /**
     * Returns an easier to read representation of the supplied qualified
     * zone id: <code>type:id</code>.
     */
    public static String toString (int qualifiedZoneId)
    {
        return zoneType(qualifiedZoneId) + ":" + zoneId(qualifiedZoneId);
    }
}
