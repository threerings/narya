//
// $Id: CharacterManager.java,v 1.11 2001/12/17 03:33:40 mdb Exp $

package com.threerings.cast;

import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;

import com.samskivert.util.Tuple;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.sprite.Sprite;

import com.threerings.cast.Log;
import com.threerings.cast.util.TileUtil;

/**
 * The character manager provides facilities for constructing sprites that
 * are used to represent characters in a scene. It also handles the
 * compositing and caching of composited character animations.
 */
public class CharacterManager
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
     * Returns a {@link CharacterSprite} representing the character
     * described by the given {@link CharacterDescriptor}, or
     * <code>null</code> if an error occurs.
     *
     * @param desc the character descriptor.
     */
    public CharacterSprite getCharacter (CharacterDescriptor desc)
    {
        try {
            CharacterSprite sprite = (CharacterSprite)
                _charClass.newInstance();
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
    protected MultiFrameImage[] getActionFrames (
        CharacterDescriptor descrip, String action)
        throws NoSuchComponentException
    {
        // the cache is keyed on both values
        Tuple key = new Tuple(descrip, action);
        MultiFrameImage[] frames = (MultiFrameImage[])_frames.get(key);
        if (frames == null) {
            // do the compositing
            frames = createCompositeFrames(descrip, action);
            // cache the result
            _frames.put(key, frames);
        }
        return frames;
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
    protected MultiFrameImage[] createCompositeFrames (
        CharacterDescriptor descrip, String action)
        throws NoSuchComponentException
    {
        MultiFrameImage[] frames = new MultiFrameImage[Sprite.DIRECTION_COUNT];

        // obtain the necessary components
        int[] cids = descrip.getComponentIds();
        int ccount = cids.length;
        CharacterComponent[] components = new CharacterComponent[ccount];
        for (int i = 0; i < ccount; i++) {
            components[i] = _crepo.getComponent(cids[i]);
        }

        // sort them into the proper rendering order
        Arrays.sort(components, ComponentClass.RENDER_COMP);

        // now composite the component frames, one atop the next
        for (int i = 0; i < ccount; i++) {
            TileUtil.compositeFrames(frames, components[i].getFrames(action));
        }

        return frames;
    }

    /** The component repository. */
    protected ComponentRepository _crepo;

    /** A table of our action sequences. */
    protected HashMap _actions = new HashMap();

    /** A cache of composited animation frames. */
    protected HashMap _frames = new HashMap();

    /** The character class to be created. */
    protected Class _charClass = CharacterSprite.class;
}
