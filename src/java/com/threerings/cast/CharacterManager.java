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

package com.threerings.cast;

import java.util.Iterator;
import java.util.HashMap;

import com.samskivert.util.LRUHashMap;
import com.samskivert.util.RuntimeAdjust;
import com.samskivert.util.Throttle;
import com.samskivert.util.Tuple;

import com.threerings.media.image.Colorization;
import com.threerings.media.image.ImageManager;
import com.threerings.util.DirectionCodes;

import com.threerings.cast.CompositedActionFrames.ComponentFrames;
import com.threerings.cast.Log;

/**
 * The character manager provides facilities for constructing sprites that
 * are used to represent characters in a scene. It also handles the
 * compositing and caching of composited character animations.
 */
public class CharacterManager
    implements DirectionCodes
{
    /**
     * Constructs the character manager.
     */
    public CharacterManager (ImageManager imgr, ComponentRepository crepo)
    {
        // keep these around
        _imgr = imgr;
        _crepo = crepo;

        // populate our actions table
        Iterator iter = crepo.enumerateActionSequences();
        while (iter.hasNext()) {
            ActionSequence action = (ActionSequence)iter.next();
            _actions.put(action.name, action);
        }

        // create a cache for our composited action frames
        int acsize = _cacheSize.getValue();
        Log.debug("Creating action cache [size=" + acsize + "k].");
        _frameCache = new LRUHashMap(acsize*1024, new LRUHashMap.ItemSizer() {
            public int computeSize (Object value) {
                return (int)((CompositedMultiFrameImage)
                             value).getEstimatedMemoryUsage();
            }
        });
        _frameCache.setTracking(true); // TODO
    }

    /**
     * Returns the component repository being used by this manager.
     */
    public ComponentRepository getComponentRepository ()
    {
        return _crepo;
    }

    /**
     * Instructs the character manager to construct instances of this
     * derived class of {@link CharacterSprite} when creating new sprites.
     *
     * @exception IllegalArgumentException thrown if the supplied class
     * does not derive from {@link CharacterSprite}.
     */
    public void setCharacterClass (Class charClass)
    {
        // sanity check
        if (!CharacterSprite.class.isAssignableFrom(charClass)) {
            String errmsg = "Requested to use character sprite class that " + 
                "does not derive from CharacterSprite " +
                "[class=" + charClass.getName() + "].";
            throw new IllegalArgumentException(errmsg);
        }

        // make a note of it
        _charClass = charClass;
    }

    /**
     * Instructs the character manager to use the provided cache for
     * composited action animations.
     */
    public void setActionCache (ActionCache cache)
    {
        _acache = cache;
    }

    /**
     * Returns a {@link CharacterSprite} representing the character
     * described by the given {@link CharacterDescriptor}, or
     * <code>null</code> if an error occurs.
     *
     * @param desc the character descriptor.
     */
    public CharacterSprite getCharacter (CharacterDescriptor desc)
    {
        return getCharacter(desc, _charClass);
    }

    /**
     * Returns a {@link CharacterSprite} representing the character
     * described by the given {@link CharacterDescriptor}, or
     * <code>null</code> if an error occurs.
     *
     * @param desc the character descriptor.
     * @param charClass the {@link CharacterSprite} derived class that
     * should be instantiated instead of the configured default (which is
     * set via {@link #setCharacterClass}).
     */
    public CharacterSprite getCharacter (CharacterDescriptor desc,
                                         Class charClass)
    {
        try {
            CharacterSprite sprite = (CharacterSprite)
                charClass.newInstance();
            sprite.init(desc, this);
            return sprite;

        } catch (Exception e) {
            Log.warning("Failed to instantiate character sprite " +
                        "[e=" + e + "].");
            Log.logStackTrace(e);
            return null;
        }
    }

    /**
     * Informs the character manager that the action sequence for the
     * given character descriptor is likely to be needed in the near
     * future and so any efforts that can be made to load it into the
     * action sequence cache in advance should be undertaken.
     *
     * <p> This will eventually be revamped to spiffily load action
     * sequences in the background.
     */
    public void resolveActionSequence (CharacterDescriptor desc, String action)
    {
        try {
            if (getActionFrames(desc, action) == null) {
                Log.warning("Failed to resolve action sequence " +
                            "[desc=" + desc + ", action=" + action + "].");
            }

        } catch (NoSuchComponentException nsce) {
            Log.warning("Failed to resolve action sequence " +
                        "[nsce=" + nsce + "].");
        }
    }

    /**
     * Returns the action sequence instance with the specified name or
     * null if no such sequence exists.
     */
    protected ActionSequence getActionSequence (String action)
    {
        return (ActionSequence)_actions.get(action);
    }

    /**
     * Obtains the composited animation frames for the specified action
     * for a character with the specified descriptor. The resulting
     * composited animation will be cached.
     *
     * @exception NoSuchComponentException thrown if any of the components
     * in the supplied descriptor do not exist.
     * @exception IllegalArgumentException thrown if any of the components
     * referenced in the descriptor do not support the specified action.
     */
    protected ActionFrames getActionFrames (
        CharacterDescriptor descrip, String action)
        throws NoSuchComponentException
    {
        Tuple key = new Tuple(descrip, action);
        ActionFrames frames = (ActionFrames)_actionFrames.get(key);
        if (frames == null) {
            // this doesn't actually composite the images, but prepares an
            // object to be able to do so
            frames = createCompositeFrames(descrip, action);
            _actionFrames.put(key, frames);
        }

        // periodically report our frame image cache performance
        if (!_cacheStatThrottle.throttleOp()) {
            long size = getEstimatedCacheMemoryUsage();
            int[] eff = _frameCache.getTrackedEffectiveness();
            Log.debug("CharacterManager LRU [mem=" + (size / 1024) + "k" +
                      ", size=" + _frameCache.size() + ", hits=" + eff[0] +
                      ", misses=" + eff[1] + "].");
        }

        return frames;
    }

    /**
     * Returns the estimated memory usage in bytes for all images
     * currently cached by the cached action frames.
     */
    protected long getEstimatedCacheMemoryUsage ()
    {
        long size = 0;
        Iterator iter = _frameCache.values().iterator();
        while (iter.hasNext()) {
            size += ((CompositedMultiFrameImage)
                     iter.next()).getEstimatedMemoryUsage();
        }
        return size;
    }

    /**
     * Generates the composited animation frames for the specified action
     * for a character with the specified descriptor.
     *
     * @exception NoSuchComponentException thrown if any of the components
     * in the supplied descriptor do not exist.
     * @exception IllegalArgumentException thrown if any of the components
     * referenced in the descriptor do not support the specified action.
     */
    protected ActionFrames createCompositeFrames (
        CharacterDescriptor descrip, String action)
        throws NoSuchComponentException
    {
        int[] cids = descrip.getComponentIds();
        int ccount = cids.length;
        Colorization[][] zations = descrip.getColorizations();

        Log.debug("Compositing action [action=" + action +
                  ", descrip=" + descrip + "].");

        // create colorized versions of all of the source action frames
        ComponentFrames[] sources = new ComponentFrames[ccount];
        for (int ii = 0; ii < ccount; ii++) {
            sources[ii] = new ComponentFrames();
            sources[ii].ccomp = _crepo.getComponent(cids[ii]);
            ActionFrames source = sources[ii].ccomp.getFrames(action);
            if (source == null) {
                String errmsg = "Cannot composite action frames; no such " +
                    "action for component [action=" + action +
                    ", desc=" + descrip + ", comp=" + sources[ii].ccomp + "]";
                throw new RuntimeException(errmsg);
            }
            sources[ii].frames = (zations == null || zations[ii] == null) ?
                source : source.cloneColorized(zations[ii]);
        }

        // use those to create an entity that will lazily composite things
        // together as they are needed
        return new CompositedActionFrames(_imgr, _frameCache, action, sources);
    }

    /** The image manager with whom we interact. */
    protected ImageManager _imgr;

    /** The component repository. */
    protected ComponentRepository _crepo;

    /** A table of our action sequences. */
    protected HashMap _actions = new HashMap();

    /** A table of composited action sequences (these don't reference the
     * actual image data directly and thus take up little memory). */
    protected HashMap _actionFrames = new HashMap();

    /** A cache of composited animation frames. */
    protected LRUHashMap _frameCache;

    /** The character class to be created. */
    protected Class _charClass = CharacterSprite.class;

    /** The action animation cache, if we have one. */
    protected ActionCache _acache;

    /** Throttle our cache status logging to once every 30 seconds. */
    protected Throttle _cacheStatThrottle = new Throttle(1, 30000L);

    /** Register our image cache size with the runtime adjustments
     * framework. */
    protected static RuntimeAdjust.IntAdjust _cacheSize =
        new RuntimeAdjust.IntAdjust(
            "Size (in kb of memory used) of the character manager LRU " +
            "action cache [requires restart]", "narya.cast.action_cache_size",
            CastPrefs.config, 1024);
}
