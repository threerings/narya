//
// $Id: XMLComponentRepository.java,v 1.2 2001/10/30 16:16:01 shaper Exp $

package com.threerings.miso.scene.xml;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import com.samskivert.util.Config;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Tuple;

import com.threerings.cast.*;
import com.threerings.cast.CharacterComponent.ComponentFrames;

import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;
import com.threerings.miso.util.MisoUtil;

/**
 * The xml file component repository provides access to character
 * components obtained from component descriptions stored in an XML
 * file.
 */
public class XMLFileComponentRepository implements ComponentRepository
{
    /**
     * Constructs an xml file component repository.
     */
    public XMLFileComponentRepository (Config config, TileManager tilemgr)
    {
        // save off our objects
        _tilemgr = tilemgr;

        // load component types and components
        String file = config.getValue(COMPFILE_KEY, DEFAULT_COMPFILE);
        try {
            XMLComponentParser p = new XMLComponentParser();
            p.loadComponents(file, _types, _classes, _components);
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
        Tuple cinfo = (Tuple)_components.get(cid);
        if (cinfo == null) {
            throw new NoSuchComponentException(cid);
        }

        // get the component type
        int ctid = ((Integer)cinfo.left).intValue();
        ComponentType type = (ComponentType)_types.get(ctid);

        // get the component class
        int clid = ((Integer)cinfo.right).intValue();
        ComponentClass cclass = (ComponentClass)_classes.get(clid);

        // get the character animation images
        ComponentFrames frames = TileUtil.getComponentFrames(
            _tilemgr, cid, type.frameCount);

        // create the component
        return new CharacterComponent(type, cclass, cid, frames);
    }

    // documentation inherited
    public Iterator enumerateComponentTypes ()
    {
        return Collections.unmodifiableMap(_types).values().iterator();
    }

    // documentation inherited
    public Iterator enumerateComponentsByType (int ctid)
    {
        return new ComponentIterator(ctid);
    }

    // documentation inherited
    public Iterator enumerateComponentClasses ()
    {
        return Collections.unmodifiableMap(_classes).values().iterator();
    }

    // documentation inherited
    public Iterator enumerateComponentsByClass (int ctid, int clid)
    {
        return new ComponentIterator(ctid, clid);
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
         * the specified component type.
         */
        public ComponentIterator (int ctid)
        {
            init(ctid, -1);
        }

        /**
         * Constructs an iterator that iterates over all components of
         * the specified component type and class.
         */
        public ComponentIterator (int ctid, int clid)
        {
            init(ctid, clid);
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

        protected void init (int ctid, int clid)
        {
            _ctid = ctid;
            _clid = clid;
            _iter = _components.keys();
            advance();
        }

        protected void advance ()
        {
            while (_iter.hasNext()) {
                Integer cid = (Integer)_iter.next();

                Tuple c = (Tuple)_components.get(cid);
                int ctid = ((Integer)c.left).intValue();
                int clid = ((Integer)c.right).intValue();
                if (ctid == _ctid &&
                    (_clid == -1 || (clid == _clid))) {
                    _next = cid;
                    return;
                }
            }
            _next = null;
        }

        /** The component type id we're enumerating over. */
        protected int _ctid;

        /** The component class id required for inclusion in the
         * iterator, or -1 for all classes. */
        protected int _clid;

        /** The next character component of the component type id
         * associated with this iterator, or null if no more exist. */
        protected Object _next;

        /** The iterator over all components in the repository. */
        protected Iterator _iter;
    }

    /** The config key for the character description file. */
    protected static final String COMPFILE_KEY =
        MisoUtil.CONFIG_KEY + ".components";

    /** The default character description file. */
    protected static final String DEFAULT_COMPFILE =
        "rsrc/config/miso/components.xml";

    /** The hashtable of component types. */
    protected HashIntMap _types = new HashIntMap();

    /** The hashtable of component classes. */
    protected HashIntMap _classes = new HashIntMap();

    /** The hashtable of character components. */
    protected HashIntMap _components = new HashIntMap();

    /** The tile manager. */
    protected TileManager _tilemgr;
}
