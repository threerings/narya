//
// $Id: XMLComponentRepository.java,v 1.1 2001/10/26 01:17:22 shaper Exp $

package com.threerings.miso.scene.xml;

import java.io.IOException;
import java.util.Iterator;

import com.samskivert.util.Config;
import com.samskivert.util.HashIntMap;

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
            new XMLComponentParser().loadComponents(
                file, _types, _components);
        } catch (IOException ioe) {
            Log.warning("Exception loading component descriptions " +
                        "[ioe=" + ioe + "].");
        }
    }

    // documentation inherited
    public CharacterComponent getComponent (int cid)
        throws NoSuchComponentException, NoSuchComponentTypeException
    {
        // get the component type id
        Integer ctid = (Integer)_components.get(cid);
        if (ctid == null) {
            throw new NoSuchComponentException(cid);
        }

        // get the component type
        ComponentType type = (ComponentType)_types.get(ctid.intValue());
        if (type == null) {
            throw new NoSuchComponentTypeException(ctid.intValue());
        }

        // get the character animation images
        ComponentFrames frames = TileUtil.getComponentFrames(
            _tilemgr, cid, type.frameCount);

        // create the component
        return new CharacterComponent(type, cid, frames);
    }

    // documentation inherited
    public Iterator enumerateComponents (int ctid)
        throws NoSuchComponentTypeException
    {
        return new ComponentTypeIterator(ctid);
    }

    // documentation inherited
    public Iterator enumerateComponentTypes ()
    {
        return _types.keys();
    }

    /**
     * Iterates over all components of a specified component type in
     * the component hashtable.
     */
    protected class ComponentTypeIterator implements Iterator
    {
        public ComponentTypeIterator (int ctid)
            throws NoSuchComponentTypeException
        {
            _ctid = ctid;
            _iter = _components.keys();
            advance();

            // make sure we have at least one component of the
            // specified type
            if (_next == null) {
                throw new NoSuchComponentTypeException(ctid);
            }
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
                CharacterComponent c = (CharacterComponent)_iter.next();
                if (c.getType().ctid == _ctid) {
                    _next = c;
                    return;
                }
            }
            _next = null;
        }

        /** The component type id we're enumerating over. */
        protected int _ctid;

        /** The next character component of the component type id
         * associated with this iterator, or null if no more exist. */
        protected Object _next;

        /** Iterator over the components in the repository. */
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

    /** The hashtable of character components. */
    protected HashIntMap _components = new HashIntMap();

    /** The tile manager. */
    protected TileManager _tilemgr;
}
