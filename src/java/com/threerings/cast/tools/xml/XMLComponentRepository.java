//
// $Id: XMLComponentRepository.java,v 1.5 2001/11/02 15:28:20 shaper Exp $

package com.threerings.miso.scene.xml;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import com.samskivert.util.Config;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Tuple;

import com.threerings.cast.*;
import com.threerings.cast.util.TileUtil;

import com.threerings.media.ImageManager;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;
import com.threerings.miso.util.MisoUtil;

/**
 * The xml component repository provides access to character
 * components obtained from component descriptions stored in an XML
 * file.
 */
public class XMLComponentRepository implements ComponentRepository
{
    /**
     * Constructs an xml component repository.
     */
    public XMLComponentRepository (Config config, ImageManager imgmgr)
    {
        // load component types and components
        String file = config.getValue(COMPONENTS_KEY, DEFAULT_COMPONENTS);
        try {
            XMLComponentParser p = new XMLComponentParser(imgmgr);
            p.loadComponents(file, _actions, _classes, _components);
            _imagedir = p.getImageDir();

        } catch (IOException ioe) {
            Log.warning("Exception loading component descriptions " +
                        "[ioe=" + ioe + "].");
        }
    }

    // documentation inherited
    public CharacterComponent getComponent (int cid)
        throws NoSuchComponentException
    {
        // get the component information
        CharacterComponent c = (CharacterComponent)_components.get(cid);
        if (c == null) {
            throw new NoSuchComponentException(cid);
        }

        // get the character animation frames
        c.setFrames(TileUtil.getComponentFrames(_imagedir, c));

        return c;
    }

    // documentation inherited
    public Iterator enumerateComponentClasses ()
    {
        return Collections.unmodifiableMap(_classes).values().iterator();
    }

    // documentation inherited
    public Iterator enumerateComponentsByClass (int clid)
    {
        return new ComponentIterator(clid);
    }

    /**
     * Iterates over all components of a specified component type, and
     * optionally a specified component class, in the component
     * hashtable.
     */
    protected class ComponentIterator implements Iterator
    {
        /**
         * Constructs an iterator that iterates over all components of
         * the specified component class.
         */
        public ComponentIterator (int clid)
        {
            _clid = clid;
            _iter = _components.keys();
            advance();
        }

        public boolean hasNext ()
        {
            return (_next != null);
        }

        public Object next ()
        {
            Object next = _next;
            advance();
            return next;
        }

        public void remove ()
        {
            throw new UnsupportedOperationException();
        }

        protected void advance ()
        {
            while (_iter.hasNext()) {
                Integer cid = (Integer)_iter.next();

                CharacterComponent c =
                    (CharacterComponent)_components.get(cid);
                if (c.getComponentClass().clid == _clid) {
                    _next = cid;
                    return;
                }
            }
            _next = null;
        }

        /** The component class id for inclusion in the iterator. */
        protected int _clid;

        /** The next character component associated with this
         * iterator, or null if no more exist. */
        protected Object _next;

        /** The iterator over all components in the repository. */
        protected Iterator _iter;
    }

    /** The config key for the character description file. */
    protected static final String COMPONENTS_KEY =
        MisoUtil.CONFIG_KEY + ".components";

    /** The default character description file. */
    protected static final String DEFAULT_COMPONENTS =
        "rsrc/config/miso/components.xml";

    /** The image directory containing the component image files. */
    protected String _imagedir;

    /** The hashtable of component types. */
    protected HashIntMap _actions = new HashIntMap();

    /** The hashtable of component classes. */
    protected HashIntMap _classes = new HashIntMap();

    /** The hashtable of character components. */
    protected HashIntMap _components = new HashIntMap();
}
