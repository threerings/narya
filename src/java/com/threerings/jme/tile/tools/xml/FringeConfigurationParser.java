//
// $Id: FringeConfigurationParser.java 3254 2004-11-30 20:03:47Z mdb $
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

package com.threerings.jme.tile.tools.xml;

import java.io.Serializable;

import org.apache.commons.digester.Digester;

import com.samskivert.util.StringUtil;
import com.samskivert.xml.SetPropertyFieldsRule;

import com.threerings.tools.xml.CompiledConfigParser;

import com.threerings.jme.Log;
import com.threerings.jme.tile.FringeConfiguration.TileRecord;
import com.threerings.jme.tile.FringeConfiguration.FringeRecord;
import com.threerings.jme.tile.FringeConfiguration;

/**
 * Parses fringe config definitions, which look like so (with angle
 * brackets instead of square):
 * <pre>
 * [fringe]
 *   [tile type="water" priority="100"]
 *      [fringe name="water_fringe_1"/]
 *      [fringe name="water_fringe_2"/]
 *      [fringe name="water_fringe_3"/]
 *   [/tile]
 *   [tile type="dirt" priority="10"]
 *      [fringe name="dirt_fringe_1" mask="true"/]
 *   [/tile]
 *   [tile type="cobble" priority="100"]
 *      [fringe name="cobble_fringe_1" mask="false"/]
 *   [/tile]
 * [/fringe]
 * </pre>
 */
public class FringeConfigurationParser extends CompiledConfigParser
{
    // documentation inherited
    protected Serializable createConfigObject ()
    {
        return new FringeConfiguration();
    }

    // documentation inherited
    protected void addRules (Digester digest)
    {
        // configure top-level constraints
        String prefix = "fringe";
        digest.addRule(prefix, new SetPropertyFieldsRule());

        // create and configure fringe config instances
        prefix += "/tile";
        digest.addObjectCreate(prefix, TileRecord.class.getName());
        digest.addRule(prefix, new SetPropertyFieldsRule());
        digest.addSetNext(prefix, "addTileRecord");

        // create the fringe type records in each tile record
        prefix += "/fringe";
        digest.addObjectCreate(prefix, FringeRecord.class.getName());
        digest.addRule(prefix, new SetPropertyFieldsRule());
        digest.addSetNext(prefix, "addFringe");
    }
}
