//
// $Id: XMLTileSetParserTest.java,v 1.4 2002/02/09 07:50:04 mdb Exp $

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
