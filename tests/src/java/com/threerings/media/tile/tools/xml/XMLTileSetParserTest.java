//
// $Id: XMLTileSetParserTest.java,v 1.2 2001/11/29 21:55:56 mdb Exp $

package com.threerings.media.tools.tile.xml;

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
                System.out.println(iter.next());
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
        "rsrc/media/tools/tile/xml/tilesets.xml";
}
