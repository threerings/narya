//
// $Id: CharacterManager.java,v 1.7 2001/11/01 01:40:42 shaper Exp $

package com.threerings.cast;

import java.util.*;

import com.samskivert.util.CollectionUtil;

import com.threerings.cast.Log;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.sprite.Sprite;

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
        CharacterComponent components[] = getComponents(desc.getComponents());
        if (components.length == 0) {
            Log.warning("No character components in descriptor.");
            return null;
        }
        // assume all components support the same set of action sequences
        ActionSequence seqs[] = components[0].getActionSequences();

        // create the composite character image
        MultiFrameImage frames[][] =
            createCompositeFrames(seqs.length, components);
        if (frames == null) {
            return null;
        }

        // instantiate the character sprite
        CharacterSprite sprite = createSprite();
        if (sprite == null) {
            return null;
        }

        // populate the character sprite with its attributes
        sprite.setAnimations(seqs, frames);

        long end = System.currentTimeMillis();
        Log.info("Generated character sprite [ms=" + (end - start) + "].");

        return sprite;
    }

    /**
     * Returns an iterator over the {@link ComponentClass} objects
     * representing all available character component classes.
     */
    public Iterator enumerateComponentClasses ()
    {
        return _repo.enumerateComponentClasses();
    }

    /**
     * Returns an iterator over the <code>Integer</code> objects
     * representing all available character component identifiers for
     * the given character component class identifier.
     */
    public Iterator enumerateComponentsByClass (int clid)
    {
        return _repo.enumerateComponentsByClass(clid);
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
     * Returns an array of the character component objects specified
     * in the given array of component ids.
     */
    protected CharacterComponent[] getComponents (int cids[])
    {
        int size = cids.length;
        CharacterComponent components[] = new CharacterComponent[size];

        try {
            for (int ii = 0; ii < size; ii++) {
                components[ii] = _repo.getComponent(cids[ii]);
            }

        } catch (NoSuchComponentException nsce) {
            Log.warning("Exception retrieving character component " +
                        "[nsce=" + nsce + "].");
            return null;
        }

        return components;
    }

    /**
     * Returns an array of multi frame images containing the fully
     * composited images for the given action sequences and
     * components.
     */
    protected MultiFrameImage[][] createCompositeFrames (
        int seqCount, CharacterComponent components[])
    {
        MultiFrameImage frames[][] =
            new MultiFrameImage[seqCount][Sprite.NUM_DIRECTIONS];

        // render all component frames one atop another
        for (int ii = 0; ii < _renderRank.length; ii++) {
            int clidx = _renderRank[ii].clid;
            Log.info("Compositing component [c=" + components[clidx] + "].");
            TileUtil.compositeFrames(frames, components[clidx].getFrames());
        }

        return frames;
    }

    /**
     * Returns a new instance of the {@link CharacterSprite}-derived
     * class specified for use by this character manager.
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
