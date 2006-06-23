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

import com.threerings.io.Streamable;

/**
 * The scene summary class is used to provide info about the connected
 * group of scenes that make up a zone. The group of scenes that make up a
 * zone is a self-contained set of scenes, connected with one another (by
 * portals) but not to any scenes outside the group.
 */
public class SceneSummary implements Streamable
{
    /** The id of this scene. */
    public int sceneId;

    /** The name of this scene. */
    public String name;

    /** The ids of the scenes to which this scene is connected via
     * portals. */
    public int[] neighbors;

    /** The directions in which each of the neighbors lay. */
    public int[] neighborDirs;

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return "[sceneId=" + sceneId + ", name=" + name +
            ", neighbors=" + StringUtil.toString(neighbors) +
            ", neighborDirs=" + StringUtil.toString(neighborDirs) + "]";
    }
}
