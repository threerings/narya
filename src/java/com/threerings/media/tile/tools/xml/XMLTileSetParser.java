//
// $Id: XMLTileSetParser.java,v 1.7 2002/03/28 00:56:20 mdb Exp $

package com.threerings.media.tile.tools.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.SAXException;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

import com.samskivert.util.ConfigUtil;
import com.samskivert.xml.ValidatedSetNextRule;

import com.threerings.media.Log;
import com.threerings.media.tile.TileSet;

/**
 * Parse an XML tileset description file and construct tileset objects for
 * each valid description.  Does not currently perform validation on the
 * input XML stream, though the parsing code assumes the XML document is
 * well-formed.
 */
public class XMLTileSetParser
{
    /**
     * Constructs an xml tile set parser.
     */
    public XMLTileSetParser ()
    {
        // create our digester
        _digester = new Digester();
    }

    /**
     * Adds a ruleset to be used when parsing tiles. This should be an
     * instance of a class derived from {@link TileSetRuleSet}. The prefix
     * will be used to configure the ruleset so that it matches elements
     * at a particular point in the XML hierarchy. For example:
     *
     * <pre>
     * _parser.addRuleSet("tilesets", new UniformTileSetRuleSet());
     * </pre>
     */
    public void addRuleSet (String prefix, TileSetRuleSet ruleset)
    {
        // configure the ruleset with the appropriate prefix
        ruleset.setPrefix(prefix);

        // and have it set itself up with the digester
        _digester.addRuleSet(ruleset);

        // add a set next rule which will put tilesets with this prefix
        // into the array list that'll be on the top of the stack
        _digester.addRule(prefix + TileSetRuleSet.TILESET_PATH,
                          new ValidatedSetNextRule(_digester, 
                                                   "add", Object.class,
                                                   ruleset));
    }

    /**
     * Loads all of the tilesets specified in the supplied XML tileset
     * description file and places them into the supplied hashmap indexed
     * by tileset name. This method is not reentrant, so don't go calling
     * it from multiple threads.
     *
     * @param path a path, relative to the classpath, at which the tileset
     * definition file can be found.
     * @param tilesets the hashmap into which the tilesets will be placed,
     * indexed by tileset name.
     */
    public void loadTileSets (String path, HashMap tilesets)
        throws IOException
    {
        // get an input stream for this XML file
        InputStream is = ConfigUtil.getStream(path);
        if (is == null) {
            String errmsg = "Can't load tileset description file from " +
                "classpath [path=" + path + "].";
            throw new FileNotFoundException(errmsg);
        }

        // load up the tilesets
        loadTileSets(is, tilesets);
    }

    /**
     * Loads all of the tilesets specified in the supplied XML tileset
     * description file and places them into the supplied hashmap indexed
     * by tileset name. This method is not reentrant, so don't go calling
     * it from multiple threads.
     *
     * @param file the file in which the tileset definition file can be
     * found.
     * @param tilesets the hashmap into which the tilesets will be placed,
     * indexed by tileset name.
     */
    public void loadTileSets (File file, HashMap tilesets)
        throws IOException
    {
        // load up the tilesets
        loadTileSets(new FileInputStream(file), tilesets);
    }

    /**
     * Loads all of the tilesets specified in the supplied XML tileset
     * description file and places them into the supplied hashmap indexed
     * by tileset name. This method is not reentrant, so don't go calling
     * it from multiple threads.
     *
     * @param source an input stream from which the tileset definition
     * file can be read.
     * @param tilesets the hashmap into which the tilesets will be placed,
     * indexed by tileset name.
     */
    public void loadTileSets (InputStream source, HashMap tilesets)
        throws IOException
    {
        // stick an array list on the top of the stack for collecting
        // parsed tilesets
        ArrayList setlist = new ArrayList();
        _digester.push(setlist);

        // now fire up the digester to parse the stream
        try {
            _digester.parse(source);
        } catch (SAXException saxe) {
            Log.warning("Exception parsing tile set descriptions " +
                        "[error=" + saxe + "].");
            Log.logStackTrace(saxe);
        }

        // stick the tilesets from the list into the hashtable
        for (int i = 0; i < setlist.size(); i++) {
            TileSet set = (TileSet)setlist.get(i);
            if (set.getName() == null) {
                Log.warning("Tileset did not receive name during " +
                            "parsing process [set=" + set + "].");
            } else {
                tilesets.put(set.getName(), set);
            }
        }

        // and clear out the list for next time
        setlist.clear();
    }

    /** Our XML digester. */
    protected Digester _digester;
}
