//
// $Id: XMLTileSetParserTest.java,v 1.5 2004/08/27 02:21:00 mdb Exp $
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

import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;

public class XMLTileSetParserTest extends TestCase
{
    public XMLTileSetParserTest ()
    {
        super(XMLTileSetParserTest.class.getName());
    }

    public void runTest ()
    {
        HashMap sets = new HashMap();

        XMLTileSetParser parser = new XMLTileSetParser();
        // add some rulesets
        parser.addRuleSet("tilesets/uniform", new UniformTileSetRuleSet());
        parser.addRuleSet("tilesets/swissarmy", new SwissArmyTileSetRuleSet());
        parser.addRuleSet("tilesets/object", new ObjectTileSetRuleSet());

        // load up the tilesets
        try {
            parser.loadTileSets(TILESET_PATH, sets);

            // print them out
            Iterator iter = sets.values().iterator();
            while (iter.hasNext()) {
                iter.next();
                // System.out.println(iter.next());
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("loadTileSets() failed");
        }
    }

    public static Test suite ()
    {
        return new XMLTileSetParserTest();
    }

    public static void main (String[] args)
    {
        XMLTileSetParserTest test = new XMLTileSetParserTest();
        test.runTest();
    }

    protected static final String TILESET_PATH =
        "rsrc/media/tile/tools/xml/tilesets.xml";
}
