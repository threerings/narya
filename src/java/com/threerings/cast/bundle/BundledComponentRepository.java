//
// $Id: BundledComponentRepository.java,v 1.11 2002/05/06 18:08:31 mdb Exp $

package com.threerings.cast.bundle;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.io.NestableIOException;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Tuple;

import org.apache.commons.collections.FilterIterator;
import org.apache.commons.collections.Predicate;

import com.threerings.resource.ResourceBundle;
import com.threerings.resource.ResourceManager;

import com.threerings.media.ImageManager;
import com.threerings.media.util.Colorization;
import com.threerings.media.util.ImageUtil;

import com.threerings.media.tile.ImageProvider;
import com.threerings.media.tile.NoSuchTileException;
import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileSet;

import com.threerings.util.DirectionCodes;

import com.threerings.cast.ActionFrames;
import com.threerings.cast.CharacterComponent;
import com.threerings.cast.ComponentClass;
import com.threerings.cast.ComponentRepository;
import com.threerings.cast.FrameProvider;
import com.threerings.cast.Log;
import com.threerings.cast.NoSuchComponentException;

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
            for (int i = 0; i < rbundles.length; i++) {
                if (_actions == null) {
                    _actions = (HashMap)BundleUtil.loadObject(
                        rbundles[i], BundleUtil.ACTIONS_PATH);
                }
                if (_actionSets == null) {
                    _actionSets = (HashMap)BundleUtil.loadObject(
                        rbundles[i], BundleUtil.ACTION_SETS_PATH);
                }
                if (_classes == null) {
                    _classes = (HashMap)BundleUtil.loadObject(
                        rbundles[i], BundleUtil.CLASSES_PATH);
                }
            }

            // now go back and load up all of the component information
            for (int i = 0; i < rbundles.length; i++) {
                HashIntMap comps = (HashIntMap)BundleUtil.loadObject(
                    rbundles[i], BundleUtil.COMPONENTS_PATH);
                if (comps == null) {
                    continue;
                }

                // create a frame provider for this bundle
                FrameProvider fprov = new ResourceBundleProvider(rbundles[i]);

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
            throw new NestableIOException(
                "Internal error unserializing metadata", cnfe);
        }
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
    protected class ResourceBundleProvider
        implements ImageProvider, FrameProvider
    {
        /**
         * Constructs an instance that will obtain image data from the
         * specified resource bundle.
         */
        public ResourceBundleProvider (ResourceBundle bundle)
        {
            _bundle = bundle;
        }

        // documentation inherited
        public Image loadImage (String path)
            throws IOException
        {
            // obtain the image data from our resource bundle
            InputStream imgin = _bundle.getResource(path);
            if (imgin == null) {
                String errmsg = "No such image in resource bundle " +
                    "[bundle=" + _bundle + ", path=" + path + "].";
                throw new FileNotFoundException(errmsg);
            }
            return _imgr.loadImage(imgin);
        }

        // documentation inherited
        public ActionFrames getFrames (
            CharacterComponent component, String action)
        {
            // get the tileset for this action
            TileSet aset = (TileSet)_actionSets.get(action);
            if (aset == null) {
                Log.warning("Can't provide animation frames for undefined " +
                            "action [action=" + action +
                            ", component=" + component + "].");
                return null;
            }

            // construct the appropriate image path for the component
            String path = component.componentClass.name + "/" +
                component.name + "/" + action +
                BundleUtil.IMAGE_EXTENSION;

            // clone the tileset with this new image path
            try {
                TileSet fset = aset.clone(path);
                fset.setImageProvider(this);
                return new TileSetFrameImage(fset);

            } catch (CloneNotSupportedException cnse) {
                Log.warning("Unable to clone tileset [action=" + action +
                            ", component=" + component +
                            ", set=" + aset + "].");
                return null;
            }
        }

        /** The resource bundle from which we obtain image data. */
        protected ResourceBundle _bundle;
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
        public TileSetFrameImage (TileSet set)
        {
            _set = set;
        }

        // documentation inherited
        public int getFrameCount ()
        {
            return _set.getTileCount() / DIRECTION_COUNT;
        }

        // documentation inherited from interface
        public int getWidth (int index)
        {
            Tile tile = getTile(index);
            return (tile == null) ? 0 : tile.getWidth();
        }

        // documentation inherited from interface
        public int getHeight (int index)
        {
            Tile tile = getTile(index);
            return (tile == null) ? 0 : tile.getHeight();
        }

        // documentation inherited from interface
        public void paintFrame (Graphics g, int index, int x, int y)
        {
            Tile tile = getTile(index);
            if (tile != null) {
                tile.paint(g, x, y);
            }
        }

        // documentation inherited from interface
        public boolean hitTest (int index, int x, int y)
        {
            Tile tile = getTile(index);
            return (tile != null) ? tile.hitTest(x, y) : false;
        }

        // documentation inherited from interface
        public void setOrientation (int orient)
        {
            _orient = orient;
        }

        // documentation inherited from interface
        public ActionFrames cloneColorized (Colorization[] zations)
        {
            TileSet zset = _set.cloneColorized(zations);
            TileSetFrameImage cframes = new TileSetFrameImage(zset);
            cframes.setOrientation(_orient);
            return cframes;
        }

        /**
         * Fetches the requested tile.
         */
        protected Tile getTile (int index)
        {
            int tileIndex = _orient * getFrameCount() + index;
            try {
                return _set.getTile(tileIndex);
            } catch (NoSuchTileException nste) {
                Log.warning("Can't extract action frame [set=" + _set +
                            ", orient=" + _orient + ", index=" + index + "].");
                return null;
            }
        }

        /** The tileset from which we obtain our frame images. */
        protected TileSet _set;

        /** The particular orientation for which we are providing
         * frames. */
        protected int _orient;
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
}
