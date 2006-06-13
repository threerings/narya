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

package com.threerings.media.tile.tools.xml;

import org.apache.commons.digester.Digester;

import com.threerings.media.Log;
import com.threerings.media.tile.UniformTileSet;

/**
 * Parses {@link UniformTileSet} instances from a tileset description. A
 * uniform tileset description looks like so:
 *
 * <pre>
 * &lt;tileset name="Sample Uniform Tileset"&gt;
 *   &lt;imagePath&gt;path/to/image.png&lt;/imagePath&gt;
 *   &lt;!-- the width of each tile in pixels --&gt;
 *   &lt;width&gt;64&lt;/width&gt;
 *   &lt;!-- the height of each tile in pixels --&gt;
 *   &lt;height&gt;48&lt;/height&gt;
 * &lt;/tileset&gt;
 * </pre>
 */
public class UniformTileSetRuleSet extends TileSetRuleSet
{
    // documentation inherited
    public void addRuleInstances (Digester digester)
    {
        super.addRuleInstances(digester);

        digester.addCallMethod(
            _prefix + TILESET_PATH + "/width", "setWidth", 0,
            new Class[] { java.lang.Integer.TYPE });
        digester.addCallMethod(
            _prefix + TILESET_PATH + "/height", "setHeight", 0,
            new Class[] { java.lang.Integer.TYPE });
    }

    // documentation inherited
    public boolean isValid (Object target)
    {
        UniformTileSet set = (UniformTileSet)target;
        boolean valid = super.isValid(target);

        // check for a <width> element
        if (set.getWidth() == 0) {
            Log.warning("Tile set definition missing valid <width> " +
                        "element [set=" + set + "].");
            valid = false;
        }

        // check for a <height> element
        if (set.getHeight() == 0) {
            Log.warning("Tile set definition missing valid <height> " +
                        "element [set=" + set + "].");
            valid = false;
        }

        return valid;
    }

    // documentation inherited
    protected Class getTileSetClass ()
    {
        return UniformTileSet.class;
    }
}
