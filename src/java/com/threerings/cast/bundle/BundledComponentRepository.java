//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.cast.bundle;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.IntIntMap;
import com.samskivert.util.Tuple;

import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.Predicate;

import com.threerings.resource.ResourceBundle;
import com.threerings.resource.ResourceManager;

import com.threerings.media.image.Colorization;
import com.threerings.media.image.FastImageIO;
import com.threerings.media.image.ImageDataProvider;
import com.threerings.media.image.ImageManager;

import com.threerings.media.tile.IMImageProvider;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TrimmedTile;

import com.threerings.util.DirectionCodes;

import com.threerings.cast.ActionFrames;
import com.threerings.cast.ActionSequence;
import com.threerings.cast.CharacterComponent;
import com.threerings.cast.ComponentClass;
import com.threerings.cast.ComponentRepository;
import com.threerings.cast.FrameProvider;
import com.threerings.cast.Log;
import com.threerings.cast.NoSuchComponentException;
import com.threerings.cast.StandardActions;
import com.threerings.cast.TrimmedMultiFrameImage;

/**
 * A component repository implementation that obtains information from
 * resource bundles.
 *
 * @see ResourceManager
 */
public class BundledComponentRepository
    implements DirectionCodes, ComponentRepository
{
    /**
     * Constructs a repository which will obtain its resource set from the
     * supplied resource manager.
     *
     * @param rmgr the resource manager from which to obtain our resource
     * set.
     * @param imgr the image manager that we'll use to decode and cache
     * images.
     * @param name the name of the resource set from which we will be
     * loading our component data.
     *
     * @exception IOException thrown if an I/O error occurs while reading
     * our metadata from the resource bundles.
     */
    public BundledComponentRepository (
        ResourceManager rmgr, ImageManager imgr, String name)
        throws IOException
    {
        // keep this guy around
        _imgr = imgr;

        // first we obtain the resource set from whence will come our
        // bundles
        ResourceBundle[] rbundles = rmgr.getResourceSet(name);

        // look for our metadata info in each of the bundles
        try {
            int rcount = (rbundles == null) ? 0 : rbundles.length;
            for (int i = 0; i < rcount; i++) {
                if (_actions == null) {
                    _actions = (HashMap)BundleUtil.loadObject(
                        rbundles[i], BundleUtil.ACTIONS_PATH, true);
                }
                if (_actionSets == null) {
                    _actionSets = (HashMap)BundleUtil.loadObject(
                        rbundles[i], BundleUtil.ACTION_SETS_PATH, true);
                }
                if (_classes == null) {
                    _classes = (HashMap)BundleUtil.loadObject(
                        rbundles[i], BundleUtil.CLASSES_PATH, true);
                }
            }

            // now go back and load up all of the component information
            for (int i = 0; i < rcount; i++) {
                HashIntMap comps = null;
                comps = (HashIntMap)BundleUtil.loadObject(
                    rbundles[i], BundleUtil.COMPONENTS_PATH, true);
                if (comps == null) {
                    continue;
                }

                // create a frame provider for this bundle
                FrameProvider fprov =
                    new ResourceBundleProvider(_imgr, rbundles[i]);

                // now create character component instances for each component
                // in the serialized table
                Iterator iter = comps.keySet().iterator();
                while (iter.hasNext()) {
                    int componentId = ((Integer)iter.next()).intValue();
                    Tuple info = (Tuple)comps.get(componentId);
                    createComponent(componentId, (String)info.left,
                                    (String)info.right, fprov);
                }
            }

        } catch (ClassNotFoundException cnfe) {
            throw (IOException) new IOException(
                "Internal error unserializing metadata").initCause(cnfe);
        }

        // if we failed to load our classes or actions, create empty
        // hashtables so that we can safely enumerate our emptiness
        if (_actions == null) {
            _actions = new HashMap();
        }
        if (_classes == null) {
            _classes = new HashMap();
        }
    }

    /**
     * Configures the bundled component repository to wipe any bundles
     * that report certain kinds of failure. In the event that an unpacked
     * bundle becomes corrupt, this is useful in that it will force the
     * bundle to be unpacked on the next application invocation,
     * potentially remedying the problem of a corrupt unpacking.
     */
    public void setWipeOnFailure (boolean wipeOnFailure)
    {
        _wipeOnFailure = true;
    }

    // documentation inherited
    public CharacterComponent getComponent (int componentId)
        throws NoSuchComponentException
    {
        CharacterComponent component = (CharacterComponent)
            _components.get(componentId);
        if (component == null) {
            throw new NoSuchComponentException(componentId);
        }
        return component;
    }

    // documentation inherited
    public CharacterComponent getComponent (String className, String compName)
        throws NoSuchComponentException
    {
        // look up the list for that class
        ArrayList comps = (ArrayList)_classComps.get(className);
        if (comps != null) {
            // scan the list for the named component
            int ccount = comps.size();
            for (int i = 0; i < ccount; i++) {
                CharacterComponent comp = (CharacterComponent)comps.get(i);
                if (comp.name.equals(compName)) {
                    return comp;
                }
            }
        }
        throw new NoSuchComponentException(className, compName);
    }

    // documentation inherited
    public ComponentClass getComponentClass (String className)
    {
        return (ComponentClass)_classes.get(className);
    }

    // documentation inherited
    public Iterator enumerateComponentClasses ()
    {
        return _classes.values().iterator();
    }

    // documentation inherited
    public Iterator enumerateActionSequences ()
    {
        return _actions.values().iterator();
    }

    // documentation inherited
    public Iterator enumerateComponentIds (final ComponentClass compClass)
    {
        Predicate classP = new Predicate() {
            public boolean evaluate (Object input) {
                CharacterComponent comp = (CharacterComponent)
                    _components.get(input);
                return comp.componentClass.equals(compClass);
            }
        };
        return new FilterIterator(_components.keySet().iterator(), classP);
    }

    /**
     * Creates a component and inserts it into the component table.
     */
    protected void createComponent (
        int componentId, String cclass, String cname, FrameProvider fprov)
    {
        // look up the component class information
        ComponentClass clazz = (ComponentClass)_classes.get(cclass);
        if (clazz == null) {
            Log.warning("Non-existent component class " +
                        "[class=" + cclass + ", name=" + cname +
                        ", id=" + componentId + "].");
            return;
        }

        // create the component
        CharacterComponent component = new CharacterComponent(
            componentId, cname, clazz, fprov);

        // stick it into the appropriate tables
        _components.put(componentId, component);

        // we have a hash of lists for mapping components by class/name
        ArrayList comps = (ArrayList)_classComps.get(cclass);
        if (comps == null) {
            comps = new ArrayList();
            _classComps.put(cclass, comps);
        }
        if (!comps.contains(component)) {
            comps.add(component);
        } else {
            Log.info("Requested to register the same component twice? " +
                     "[component=" + component + "].");
        }
    }

    /**
     * Instances of these provide images to our component action tilesets
     * and frames to our components.
     */
    protected class ResourceBundleProvider extends IMImageProvider
        implements ImageDataProvider, FrameProvider
    {
        /**
         * Constructs an instance that will obtain image data from the
         * specified resource bundle.
         */
        public ResourceBundleProvider (ImageManager imgr, ResourceBundle bundle)
        {
            super(imgr, (String)null);
            _dprov = this;
            _bundle = bundle;
        }

        // documentation inherited from interface
        public String getIdent ()
        {
            return "bcr:" + _bundle.getSource();
        }

        // documentation inherited from interface
        public BufferedImage loadImage (String path) throws IOException
        {
            return FastImageIO.read(_bundle.getResourceFile(path));
        }

        // documentation inherited
        public ActionFrames getFrames (
            CharacterComponent component, String action, String type)
        {
            // obtain the action sequence definition for this action
            ActionSequence actseq = (ActionSequence)_actions.get(action);
            if (actseq == null) {
                Log.warning("Missing action sequence definition " +
                            "[action=" + action +
                            ", component=" + component + "].");
                return null;
            }

            // determine our image path name
            String imgpath = action, dimgpath = ActionSequence.DEFAULT_SEQUENCE;
            if (type != null) {
                imgpath += "_" + type;
                dimgpath += "_" + type;
            }

            String root = component.componentClass.name + "/" +
                component.name + "/";
            String cpath = root + imgpath + BundleUtil.TILESET_EXTENSION;
            String dpath = root + dimgpath + BundleUtil.TILESET_EXTENSION;

            // look to see if this tileset is already cached (as the
            // custom action or the default action)
            TileSet aset = (TileSet)_setcache.get(cpath);
            if (aset == null) {
                aset = (TileSet)_setcache.get(dpath);
                if (aset != null) {
                    // save ourselves a lookup next time
                    _setcache.put(cpath, aset);
                }
            }

            try {
                // then try loading up a tileset customized for this action
                if (aset == null) {
                    aset = (TileSet)BundleUtil.loadObject(
                        _bundle, cpath, false);
                }

                // if that failed, try loading the default tileset
                if (aset == null) {
                    aset = (TileSet)BundleUtil.loadObject(
                        _bundle, dpath, false);
                    _setcache.put(dpath, aset);
                }

                // if that failed too, we're hosed
                if (aset == null) {
                    // if this is a shadow image, no need to freak out as they
                    // are optional
                    if (!StandardActions.SHADOW_TYPE.equals(type)) {
                        Log.warning("Unable to locate tileset for action '" +
                                    imgpath + "' " + component + ".");
                        if (_wipeOnFailure) {
                            _bundle.wipeBundle(false);
                        }
                    }
                    return null;
                }

                aset.setImageProvider(this);
                _setcache.put(cpath, aset);
                return new TileSetFrameImage(aset, actseq);

            } catch (Exception e) {
                Log.warning("Error loading tileset for action '" + imgpath +
                            "' " + component + ".");
                Log.logStackTrace(e);
                return null;
            }
        }

        /** The resource bundle from which we obtain image data. */
        protected ResourceBundle _bundle;

        /** Cache of tilesets loaded from our bundle. */
        protected HashMap _setcache = new HashMap();
    }

    /**
     * Used to provide multiframe images using data obtained from a
     * tileset.
     */
    protected static class TileSetFrameImage implements ActionFrames
    {
        /**
         * Constructs a tileset frame image with the specified tileset and
         * for the specified orientation.
         */
        public TileSetFrameImage (TileSet set, ActionSequence actseq)
        {
            _set = set;
            _actseq = actseq;

            // compute these now to avoid pointless recomputation later
            _ocount = actseq.orients.length;
            _fcount = set.getTileCount() / _ocount;

            // create our mapping from orientation to animation sequence
            // index
            for (int ii = 0; ii < _ocount; ii++) {
                _orients.put(actseq.orients[ii], ii);
            }
        }

        // documentation inherited from interface
        public int getOrientationCount ()
        {
            return _ocount;
        }

        // documentation inherited from interface
        public TrimmedMultiFrameImage getFrames (final int orient)
        {
            return new TrimmedMultiFrameImage() {
                // documentation inherited
                public int getFrameCount ()
                {
                    return _fcount;
                }

                // documentation inherited from interface
                public int getWidth (int index)
                {
                    Tile tile = getTile(orient, index);
                    return (tile == null) ? 0 : tile.getWidth();
                }

                // documentation inherited from interface
                public int getHeight (int index)
                {
                    Tile tile = getTile(orient, index);
                    return (tile == null) ? 0 : tile.getHeight();
                }

                // documentation inherited from interface
                public void paintFrame (Graphics2D g, int index, int x, int y)
                {
                    Tile tile = getTile(orient, index);
                    if (tile != null) {
                        tile.paint(g, x, y);
                    }
                }

                // documentation inherited from interface
                public boolean hitTest (int index, int x, int y)
                {
                    Tile tile = getTile(orient, index);
                    return (tile != null) ? tile.hitTest(x, y) : false;
                }

                // documentation inherited from interface
                public void getTrimmedBounds (int index, Rectangle bounds)
                {
                    Tile tile = getTile(orient, index);
                    if (tile instanceof TrimmedTile) {
                        ((TrimmedTile)tile).getTrimmedBounds(bounds);
                    } else {
                        bounds.setBounds(
                            0, 0, tile.getWidth(), tile.getHeight());
                    }
                }
            };
        }

        // documentation inherited from interface
        public int getXOrigin (int orient, int index)
        {
            return _actseq.origin.x;
        }

        // documentation inherited from interface
        public int getYOrigin (int orient, int index)
        {
            return _actseq.origin.y;
        }

        // documentation inherited from interface
        public ActionFrames cloneColorized (Colorization[] zations)
        {
            return new TileSetFrameImage(_set.clone(zations), _actseq);
        }

        /**
         * Fetches the requested tile.
         */
        protected Tile getTile (int orient, int index)
        {
            int tileIndex = _orients.get(orient) * _fcount + index;
            return _set.getTile(tileIndex);
        }

        /** The tileset from which we obtain our frame images. */
        protected TileSet _set;

        /** The action sequence for which we're providing frame images. */
        protected ActionSequence _actseq;

        /** Frame and orientation counts. */
        protected int _fcount, _ocount;

        /** A mapping from orientation code to animation sequence
         * index. */
        protected IntIntMap _orients = new IntIntMap();
    }

    /** We use the image manager to decode and cache images. */
    protected ImageManager _imgr;

    /** A table of action sequences. */
    protected HashMap _actions;

    /** A table of action sequence tilesets. */
    protected HashMap _actionSets;

    /** A table of component classes. */
    protected HashMap _classes;

    /** A table of component lists indexed on classname. */
    protected HashMap _classComps = new HashMap();

    /** The component table. */
    protected HashIntMap _components = new HashIntMap();

    /** Whether or not we wipe our bundles on any failure. */
    protected boolean _wipeOnFailure;
}
