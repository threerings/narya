//
// $Id: CharacterManager.java,v 1.28 2002/12/23 20:33:19 mdb Exp $

package com.threerings.cast;

import java.util.Iterator;
import java.util.HashMap;

import com.samskivert.util.LRUHashMap;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Throttle;
import com.samskivert.util.Tuple;

import com.threerings.media.util.Colorization;
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
    public CharacterManager (ComponentRepository crepo)
    {
        // keep this around
        _crepo = crepo;

        // populate our actions table
        Iterator iter = crepo.enumerateActionSequences();
        while (iter.hasNext()) {
            ActionSequence action = (ActionSequence)iter.next();
            _actions.put(action.name, action);
        }

        // TODO
        _frames.setTracking(true);
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
        // first check the in-memory cache; which is keyed on both values
        Tuple key = new Tuple(descrip, action);
        ActionFrames frames = (ActionFrames)_frames.get(key);

        // next check the disk cache
        if (frames == null && _acache != null) {
            frames = _acache.getActionFrames(descrip, action);
            // cache the result in memory
            _frames.put(key, frames);
        }

        // if that failed, we'll just have to generate the danged things
        if (frames == null) {
            // do the compositing
            frames = createCompositeFrames(descrip, action);
            // cache the result on disk if we've got such a cache
            if (_acache != null) {
                _acache.cacheActionFrames(descrip, action, frames);
            }
            // cache the result in memory as well
            _frames.put(key, frames);
        }

        // periodically report our action cache performance
        if (!_cacheStatThrottle.throttleOp()) {
            long size = getEstimatedCacheMemoryUsage();
            int[] eff = _frames.getTrackedEffectiveness();
            Log.debug("CharacterManager LRU " +
                      "[mem=" + (size / 1024) + "k, size=" + _frames.size() +
                      ", hits=" + eff[0] + ", misses=" + eff[1] + "].");
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
        Iterator iter = _frames.values().iterator();
        while (iter.hasNext()) {
            ActionFrames frame = (ActionFrames)iter.next();
            size += frame.getEstimatedMemoryUsage();
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
        return new CompositedActionFrames(action, sources);
    }

    /** The component repository. */
    protected ComponentRepository _crepo;

    /** A table of our action sequences. */
    protected HashMap _actions = new HashMap();

    /** A cache of composited animation frames. */
    protected LRUHashMap _frames = new LRUHashMap(ACTION_CACHE_SIZE);

    /** The character class to be created. */
    protected Class _charClass = CharacterSprite.class;

    /** The action animation cache, if we have one. */
    protected ActionCache _acache;

    /** Throttle our cache status logging to once every 30 seconds. */
    protected Throttle _cacheStatThrottle = new Throttle(1, 30000L);

    /** The number of actions to cache before we start clearing them
     * out. */
    protected static final int ACTION_CACHE_SIZE = 30;
}
