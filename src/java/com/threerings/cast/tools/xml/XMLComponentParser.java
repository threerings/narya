//
// $Id: XMLComponentParser.java,v 1.2 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast;

import java.awt.Point;
import java.io.IOException;

import org.xml.sax.*;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Tuple;
import com.samskivert.xml.SimpleParser;

/**
 * Parses an XML character component description file and populates
 * hashtables with component type, component class, and specific
 * component information.  Does not currently perform validation on
 * the input XML stream, though the parsing code assumes the XML
 * document is well-formed.
 */
public class XMLComponentParser extends SimpleParser
{
    // documentation inherited
    public void startElement (String uri, String localName,
			      String qName, Attributes attributes)
    {
        if (qName.equals("type")) {
            // construct the component type object
            ComponentType ct = new ComponentType();

            // retrieve character attributes
            ct.ctid = parseInt(attributes.getValue("ctid"));
            ct.frameCount = parseInt(attributes.getValue("frames"));
            ct.fps = parseInt(attributes.getValue("fps"));
            parsePoint(attributes.getValue("origin"), ct.origin);

            // save the component type
            _types.put(ct.ctid, ct);

        } else if (qName.equals("class")) {
            // save off the component class info
            ComponentClass cclass = new ComponentClass();
            cclass.clid = parseInt(attributes.getValue("clid"));
            cclass.name = attributes.getValue("name");
            cclass.render = parseInt(attributes.getValue("render"));
            _classes.put(cclass.clid, cclass);

        } else if (qName.equals("component")) {
            // retrieve the component attributes
            int cid = parseInt(attributes.getValue("cid"));
            int ctid = parseInt(attributes.getValue("ctid"));
            int clid = parseInt(attributes.getValue("clid"));

            // output a warning if the component references valid
            // attributes
            if (_types.get(ctid) == null) {
                Log.warning("Component references non-existent type " +
                            "[cid=" + cid + ", ctid=" + ctid + "].");
            }
            if (_classes.get(clid) == null) {
                Log.warning("Component references non-existent class " +
                            "[cid=" + cid + ", clid=" + clid + "].");
            }

            // save off the component information
            _components.put(cid, new Tuple(
                new Integer(ctid), new Integer(clid)));
        }
    }

    /**
     * Loads the component descriptions in the given file into the
     * given component data hashtables.
     */
    public void loadComponents (
        String file, HashIntMap types, HashIntMap classes,
        HashIntMap components)
        throws IOException
    {
        // save off hashtables for reference while parsing
        _types = types;
        _classes = classes;
        _components = components;

        parseFile(file);
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

    /** The hashtable of component types gathered while parsing. */
    protected HashIntMap _types;

    /** The hashtable of character components gathered while parsing. */
    protected HashIntMap _components;

    /** The hashtable of component classes gathered while parsing. */
    protected HashIntMap _classes;
}
