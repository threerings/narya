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

package com.threerings.whirled.zone.data;

import com.samskivert.util.StringUtil;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.util.Name;

/**
 * The zone summary contains information on a zone, including its name and
 * summary info on all of the scenes in this zone (which can be used to
 * generate a map of the zone on the client).
 */
public class ZoneSummary extends SimpleStreamableObject
{
    /** The zone's fully qualified unique identifier. */
    public int zoneId;

    /** The name of the zone. */
    public Name name;

    /** The summary information for all of the scenes in the zone. */
    public SceneSummary[] scenes;

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[zoneId=" + zoneId + ", name=" + name +
            ", scenes=" + StringUtil.toString(scenes) + "]";
    }
}
