//
// $Id: CharacterComponent.java,v 1.1 2001/10/26 01:17:21 shaper Exp $

package com.threerings.cast;

import com.threerings.media.sprite.MultiFrameImage;

/**
 * The character component represents a single component that can be
 * composited with other character components to comprise an image
 * representing a single monolithic character displayable in any of
 * the eight cardinal compass directions as detailed in the {@link
 * com.threerings.media.sprite.Sprite} class's direction constants.
 */
public class CharacterComponent
{
    /**
     * Constructs a character component.
     */
    public CharacterComponent (ComponentType type, int cid,
                               ComponentFrames frames)
    {
        _type = type;
        _cid = cid;
        _frames = frames;
    }
                               
    /**
     * Returns the unique component identifier.
     */
    public int getId ()
    {
        return _cid;
    }

    /**
     * Returns the display frames used to display this component.
     */
    public ComponentFrames getFrames ()
    {
        return _frames;
    }

    /**
     * Returns the {@link ComponentType} object describing the base
     * component type information associated with this component.
     */
    public ComponentType getType ()
    {
        return _type;
    }

    /**
     * Returns a string representation of this character component.
     */
    public String toString ()
    {
        return "[cid=" + _cid + ", type=" + _type + "]";
    }

    /**
     * A class to hold the standing and walking frames of animation
     * that comprise a {@link CharacterComponent} object's various
     * display images.
     */
    public static class ComponentFrames
    {
        /** The standing animations in each orientation. */
        public MultiFrameImage stand[];

        /** The walking animations in each orientation. */
        public MultiFrameImage walk[];
    }

    /** The unique character component identifier. */
    protected int _cid;

    /** The animation frames. */
    protected ComponentFrames _frames;

    /** The character component type. */
    protected ComponentType _type;
}
