//
// $Id: XMLComponentParser.java,v 1.3 2001/11/01 01:40:42 shaper Exp $

package com.threerings.cast;

import java.awt.Point;
import java.io.IOException;
import java.util.Arrays;

import org.xml.sax.*;

import com.samskivert.util.*;
import com.samskivert.xml.SimpleParser;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.*;

import com.threerings.cast.Log;

/**
 * Parses an XML character component description file and populates
 * hashtables with {@link ActionSequence}, {@link ComponentClass}, and
 * the information necessary to construct {@link CharacterComponent}
 * objects.
 *
 * <p> Does not currently perform validation on the input XML stream,
 * though the parsing code assumes the XML document is well-formed.
 */
public class XMLComponentParser extends SimpleParser
{
    /**
     * Constructs an xml component parser.
     */
    public XMLComponentParser (ImageManager imgmgr)
    {
        _imgmgr = imgmgr;
    }

    // documentation inherited
    public void startElement (
        String uri, String localName, String qName, Attributes attributes)
    {
        if (qName.equals("charactercomponents")) {
            // save off the image directory
            _imagedir = attributes.getValue("imagedir");
            // load the tile sets
            loadTileSets(attributes.getValue("tilesets"));

        } else if (qName.equals("action")) {
            // construct and save off the action sequence object
            ActionSequence as = getActionSequence(attributes);
            _actions.put(as.asid, as);

        } else if (qName.equals("class")) {
            // construct and save off the component class object
            ComponentClass cclass = getComponentClass(attributes);
            _classes.put(cclass.clid, cclass);

        } else if (qName.equals("component")) {
            // construct and save off the character component
            CharacterComponent component = getComponent(attributes);
            _components.put(component.getId(), component);
        }
    }

    /**
     * Loads the component descriptions in the given file into the
     * given component data hashtables.
     */
    public void loadComponents (String file, HashIntMap actions,
                                HashIntMap classes, HashIntMap components)
        throws IOException
    {
        // save off hashtables for reference while parsing
        _actions = actions;
        _classes = classes;
        _components = components;

        // clear out remnants of any previous parsing antics
        _imagedir = null;
        _tilesets.clear();

        // parse the file
        parseFile(file);
    }

    /**
     * Returns the image directory the component tile images reside
     * within.
     */
    public String getImageDir ()
    {
        return _imagedir;
    }

    /**
     * Loads the tile sets in the XML file at the given path into the
     * tile sets available for reference by the action sequences.
     */
    protected void loadTileSets (String path)
    {
        // make sure we have a reasonable path
        if (path == null) {
            Log.warning("Null component tile set description file path.");
            return;
        }

        // parse the tile set description file
        XMLTileSetParser p = new XMLTileSetParser(_imgmgr);
        try {
            p.loadTileSets(path, _tilesets);
        } catch (IOException e) {
            Log.warning("Exception loading component tile set descriptions " +
                        "[path=" + path + "].");
        }
    }

    /**
     * Returns a new action sequence object as described by the given
     * attributes.
     */
    protected ActionSequence getActionSequence (Attributes attrs)
    {
        ActionSequence as = new ActionSequence();

        as.asid = parseInt(attrs.getValue("asid"));
        as.name = attrs.getValue("name");
        as.fileid = attrs.getValue("fileid");

        int tsid = parseInt(attrs.getValue("tsid"));
        as.tileset = (TileSet)_tilesets.get(tsid);
        if (as.tileset == null) {
            Log.warning("Action sequence references non-existent " +
                        "tile set [asid=" + as.asid + ", tsid=" + tsid + "].");
        }

        as.fps = parseInt(attrs.getValue("fps"));
        parsePoint(attrs.getValue("origin"), as.origin);

        return as;
    }

    /**
     * Returns a new component class object as described by the given
     * attributes.
     */
    protected ComponentClass getComponentClass (Attributes attrs)
    {
        ComponentClass cclass = new ComponentClass();
        cclass.clid = parseInt(attrs.getValue("clid"));
        cclass.name = attrs.getValue("name");
        cclass.render = parseInt(attrs.getValue("render"));
        return cclass;
    }

    /**
     * Returns a new character component object as described by the
     * given attributes.
     */
    protected CharacterComponent getComponent (Attributes attrs)
    {
        // retrieve the component attributes
        int cid = parseInt(attrs.getValue("cid"));
        String fileid = attrs.getValue("fileid");
        String val = attrs.getValue("asids");
        int asids[] = StringUtil.parseIntArray(val);
        int clid = parseInt(attrs.getValue("clid"));
        ComponentClass cclass = (ComponentClass)_classes.get(clid);

        // gather the array of relevant action sequences
        ActionSequence seqs[] = getActionSequences(asids);

        // construct the component sans frames for now
        CharacterComponent component =
            new CharacterComponent(cid, fileid, seqs, cclass);

        // warn if the component has notably invalid attributes
        validateComponent(cid, asids, clid);

        return component;
    }

    /**
     * Outputs a warning regarding the given component id if the given
     * action sequence ids and component class id for that component
     * don't exist.
     */
    protected void validateComponent (int cid, int asids[], int clid)
    {
        // check the action sequence ids
        for (int ii = 0; ii < asids.length; ii++) {
            int asid = asids[ii];
            if (_actions.get(asid) == null) {
                Log.warning(
                    "Component references non-existent action sequence " +
                    "[cid=" + cid + ", asid=" + asid + "].");
            }
        }

        // check the class id
        if (_classes.get(clid) == null) {
            Log.warning("Component references non-existent class " +
                        "[cid=" + cid + ", clid=" + clid + "].");
        }
    }

    /**
     * Returns an array of action sequence objects corresponding to
     * the action sequence ids in the given array.
     */
    protected ActionSequence[] getActionSequences (int asids[])
    {
        // sort the action sequence ids for better regularity
        Arrays.sort(asids);

        // create and populate the array
        ActionSequence[] seqs = new ActionSequence[asids.length];
        for (int ii = 0; ii < asids.length; ii++) {
            seqs[ii] = (ActionSequence)_actions.get(asids[ii]);
        }

        return seqs;
    }

    /**
     * Converts a string containing values as (x, y) into the
     * corresponding integer values and populates the given point
     * object.
     *
     * @param str the point values in string format.
     * @param point the point object to populate.
     */
    protected void parsePoint (String str, Point point)
    {
        int vals[] = StringUtil.parseIntArray(str);
        point.setLocation(vals[0], vals[1]);
    }

    /** The image directory containing the component image files. */
    protected String _imagedir;

    /** The hashtable of component tile sets. */
    protected HashIntMap _tilesets = new HashIntMap();

    /** The hashtable of action sequences gathered while parsing. */
    protected HashIntMap _actions;

    /** The hashtable of component classes gathered while parsing. */
    protected HashIntMap _classes;

    /** The hashtable of character components gathered while parsing. */
    protected HashIntMap _components;

    /** The image manager. */
    protected ImageManager _imgmgr;
}
