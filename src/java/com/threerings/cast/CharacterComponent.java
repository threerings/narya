//
// $Id: CharacterComponent.java,v 1.2 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.sprite.Sprite;

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
    public CharacterComponent (
        ComponentType type, ComponentClass cclass, int cid,
        ComponentFrames frames)
    {
        _type = type;
        _cclass = cclass;
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
     * Returns the component type associated with this component.
     */
    public ComponentType getType ()
    {
        return _type;
    }

    /**
     * Returns the component class associated with this component.
     */
    public ComponentClass getComponentClass ()
    {
        return _cclass;
    }

    /**
     * Returns a string representation of this character component.
     */
    public String toString ()
    {
        return "[cid=" + _cid + ", clid=" + _cclass.clid +
            ", type=" + _type + "]";
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

        /**
         * Constructs a component frames object.
         */
        public ComponentFrames ()
        {
            stand = new MultiFrameImage[Sprite.NUM_DIRECTIONS];
            walk = new MultiFrameImage[Sprite.NUM_DIRECTIONS];
        }
    }

    /** The unique character component identifier. */
    protected int _cid;

    /** The animation frames. */
    protected ComponentFrames _frames;

    /** The component class. */
    protected ComponentClass _cclass;

    /** The character component type. */
    protected ComponentType _type;
}
