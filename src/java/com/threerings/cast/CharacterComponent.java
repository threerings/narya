//
// $Id: CharacterComponent.java,v 1.3 2001/11/01 01:40:42 shaper Exp $

package com.threerings.cast;

import com.samskivert.util.StringUtil;

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
        int cid, String fileid, ActionSequence seqs[], ComponentClass cclass)
    {
        _cid = cid;
        _fileid = fileid;
        _seqs = seqs;
        _cclass = cclass;
    }

    /**
     * Returns the unique component identifier.
     */
    public int getId ()
    {
        return _cid;
    }

    /**
     * Returns the action sequences for this component.
     */
    public ActionSequence[] getActionSequences ()
    {
        return _seqs;
    }

    /**
     * Returns the display frames used to display this component.
     */
    public MultiFrameImage[][] getFrames ()
    {
        return _frames;
    }

    /**
     * Returns the file id.
     */
    public String getFileId ()
    {
        return _fileid;
    }

    /**
     * Returns the component class associated with this component.
     */
    public ComponentClass getComponentClass ()
    {
        return _cclass;
    }

    /**
     * Sets the frames used to render this component.
     */
    public void setFrames (MultiFrameImage frames[][])
    {
        _frames = frames;
    }

    /**
     * Returns a string representation of this character component.
     */
    public String toString ()
    {
        return "[cid=" + _cid + ", clid=" + _cclass.clid +
            ", seqs=" + StringUtil.toString(_seqs) + "]";
    }

    /** The unique character component identifier. */
    protected int _cid;

    /** The file id specifier for the tile set image file name. */
    protected String _fileid;

    /** The animation frames for each action sequence and orientation. */
    protected MultiFrameImage _frames[][];

    /** The component class. */
    protected ComponentClass _cclass;

    /** The character action sequences. */
    protected ActionSequence _seqs[];
}
