//
// $Id: XMLTileSetParser.java,v 1.2 2001/11/20 04:15:44 mdb Exp $

package com.threerings.media.tools.tile.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;

import java.util.HashMap;

import org.xml.sax.SAXException;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

import com.samskivert.util.ConfigUtil;

import com.threerings.media.Log;

/**
 * Parse an XML tileset description file and construct tileset objects
 * for each valid description.  Does not currently perform validation
 * on the input XML stream, though the parsing code assumes the XML
 * document is well-formed.
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
        // _digester.setDebug(10);
    }

    /**
     * Adds a ruleset to be used when parsing tiles. This should be an
     * instance of a class derived from {@link TileSetRuleSet} which was
     * constructed to match a particular kind of tile at a particular
     * point in the XML hierarchy. For example:
     *
     * <pre>
     * _parser.addRuleSet(new UniformTileSetRuleSet("tilesets"));
     * </pre>
     */
    public void addRuleSet (TileSetRuleSet ruleset)
    {
        // provide a reference to ourselves to the ruleset
        ruleset.init(this);

        // and have it set itself up with the digester
        _digester.addRuleSet(ruleset);
    }

    /**
     * Loads all of the tilesets specified in the supplied XML tileset
     * description file and places them into the supplied hashmap indexed
     * by tileset name.
     */
    public void loadTileSets (String path, HashMap tilesets)
        throws IOException
    {
        // save off the tileset hashtable
        _tilesets = tilesets;

        // get an input stream for this XML file
        InputStream is = ConfigUtil.getStream(path);
        if (is == null) {
            String errmsg = "Can't load tileset description file from " +
                "classpath [path=" + path + "].";
            throw new FileNotFoundException(errmsg);
        }

        Log.info("Loading from " + path + ".");

        // now fire up the digester to parse the stream
        try {
            _digester.parse(is);
        } catch (SAXException saxe) {
            Log.warning("Exception parsing tile set descriptions " +
                        "[error=" + saxe + "].");
            Log.logStackTrace(saxe);
        }
    }

    /** Our XML digester. */
    protected Digester _digester;

    /** The tilesets constructed thus far. */
    protected HashMap _tilesets;

    /** Default tileset name. */
    protected static final String DEF_NAME = "Untitled";

    /** String constant denoting an object tile set. */
    protected static final String LAYER_OBJECT = "object";
}
