//
// $Id: CharacterManager.java,v 1.6 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast;

import java.util.*;

import com.samskivert.util.CollectionUtil;

import com.threerings.cast.Log;
import com.threerings.cast.CharacterComponent.ComponentFrames;

/**
 * The character manager provides facilities for constructing sprites
 * that are used to represent characters in a scene.
 */
public class CharacterManager
{
    /**
     * Constructs the character manager.
     */
    public CharacterManager (ComponentRepository repo)
    {
        // keep this around
        _repo = repo;

        // determine component class render order
        _renderRank = getRenderRank();
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
        long start = System.currentTimeMillis();

        // get the array of component ids of each class
        int components[] = desc.getComponents();

        if (components.length == 0) {
            Log.warning("Invalid number of components " +
                        "[size=" + components.length + "].");
            return null;
        }

        // create the composite character image
        ComponentFrames frames = createCompositeFrames(desc);
        if (frames == null) {
            return null;
        }

        // instantiate the character sprite
        CharacterSprite sprite = createSprite();
        if (sprite == null) {
            return null;
        }

        // populate the character sprite with its attributes
        ComponentType ctype = desc.getType();
        sprite.setFrames(frames);
        sprite.setFrameRate(ctype.fps);
        sprite.setOrigin(ctype.origin.x, ctype.origin.y);

        long end = System.currentTimeMillis();
        Log.info("Generated character sprite [ms=" + (end - start) + "].");

        return sprite;
    }

    /**
     * Returns an iterator over the {@link ComponentType} objects
     * representing all available character component type
     * identifiers.
     */
    public Iterator enumerateComponentTypes ()
    {
        return _repo.enumerateComponentTypes();
    }

    /**
     * Returns an iterator over the <code>Integer</code> objects
     * representing all available character component identifiers for
     * the given character component type identifier.
     */
    public Iterator enumerateComponentsByType (int ctid)
    {
        return _repo.enumerateComponentsByType(ctid);
    }

    /**
     * Returns an iterator over the {@link ComponentClass} objects
     * representing all available character component class
     * identifiers.
     */
    public Iterator enumerateComponentClasses ()
    {
        return _repo.enumerateComponentClasses();
    }

    /**
     * Returns an iterator over the <code>Integer</code> objects
     * representing all available character component identifiers for
     * the given character component type and class identifiers.
     */
    public Iterator enumerateComponentsByClass (int ctid, int clid)
    {
        return _repo.enumerateComponentsByClass(ctid, clid);
    }

    /**
     * Instructs the character manager to construct instances of this
     * derived class of <code>CharacterSprite</code>.
     */
    public void setCharacterClass (Class charClass)
    {
        // sanity check
        if (!CharacterSprite.class.isAssignableFrom(charClass)) {
            Log.warning("Requested to use character class that does not " + 
                        "derive from CharacterSprite " +
                        "[class=" + charClass.getName() + "].");
            return;
        }

        // make a note of it
        _charClass = charClass;
    }

    /**
     * Returns a {@link CharacterComponent.ComponentFrames} object
     * containing the fully composited images detailed in the given
     * character descriptor.
     */
    protected ComponentFrames createCompositeFrames (CharacterDescriptor desc)
    {
        int components[] = desc.getComponents();
        ComponentFrames frames = null;

        for (int ii = 0; ii < _renderRank.length; ii++) {
            try {
                int clidx = _renderRank[ii].clid;

                // get the component to render
                CharacterComponent c = _repo.getComponent(components[clidx]);

                // TODO: fix this to deal with frames of varying dimensions
                if (frames == null) {
                    int fcount = desc.getType().frameCount;
                    frames = TileUtil.createBlankFrames(c.getFrames(), fcount);
                }

                // render the frames onto the composite frames
                TileUtil.compositeFrames(frames, c.getFrames());

            } catch (NoSuchComponentException nsce) {
                Log.warning("Exception compositing character components " +
                            "[nsce=" + nsce + "].");
                return null;
            }
        }

        return frames;
    }

    /**
     * Returns a new {@link CharacterSprite} of the character class
     * specified for use by this character manager.
     */
    protected CharacterSprite createSprite ()
    {
        try {
            return (CharacterSprite)_charClass.newInstance();
        } catch (Exception e) {
            Log.warning("Failed to instantiate character sprite " +
                        "[e=" + e + "].");
            Log.logStackTrace(e);
            return null;
        }
    }

    /**
     * Returns an array of {@link ComponentClass} objects sorted into
     * the appropriate rendering order as specified by each component
     * class object.
     */
    protected ComponentClass[] getRenderRank ()
    {
        ArrayList classes = new ArrayList();
        CollectionUtil.addAll(classes, _repo.enumerateComponentClasses());

        ComponentClass rank[] = new ComponentClass[classes.size()];
        classes.toArray(rank);
        Arrays.sort(rank, ComponentClass.RENDER_COMP);

        return rank;
    }

    /** The order in which to render component classes. */
    protected ComponentClass _renderRank[];

    /** The component repository. */
    protected ComponentRepository _repo;

    /** The character class to be created. */
    protected Class _charClass = CharacterSprite.class;
}
