//
// $Id: PuzzlePrefs.java,v 1.1 2004/10/28 18:37:49 mdb Exp $
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

package com.threerings.puzzle.client;

import com.samskivert.util.Config;

/**
 * Provides access to runtime configuration parameters for this package
 * and its subpackages.
 */
public class PuzzlePrefs
{
    /** Used to load our preferences from a properties file and map them
     * to the persistent Java preferences repository. */
    public static Config config = new Config("rsrc/config/puzzle");
}
