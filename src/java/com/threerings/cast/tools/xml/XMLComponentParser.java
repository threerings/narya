//
// $Id: XMLComponentParser.java,v 1.1 2001/10/26 01:17:21 shaper Exp $

package com.threerings.cast;

import java.awt.Point;
import java.io.IOException;

import org.xml.sax.*;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;
import com.samskivert.xml.SimpleParser;

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

            Log.info("Parsed component type [ct=" + ct + "].");

            // save the component type
            _types.put(ct.ctid, ct);

        } else if (qName.equals("component")) {
            // save off the component info
            int cid = parseInt(attributes.getValue("cid"));
            int ctid = parseInt(attributes.getValue("ctid"));
            _components.put(cid, new Integer(ctid));
        }
    }

    /**
     * Loads the component descriptions in the given file into {@link
     * ComponentType} and {@link CharacterComponent} objects in the
     * given hashtables, respectively, keyed on the type and component
     * unique id, also respectively.
     */
    public void loadComponents (
        String file, HashIntMap types, HashIntMap components)
        throws IOException
    {
        // save off hashtables for reference while parsing
        _types = types;
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
}
