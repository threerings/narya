//
// $Id: LabelSprite.java,v 1.2 2002/06/20 09:01:28 shaper Exp $

package com.threerings.media.sprite;

import java.awt.Dimension;
import java.awt.Graphics2D;

import com.samskivert.swing.Label;

import com.threerings.media.Log;

/**
 * A sprite that uses a label to render itself.  Assumes that the label
 * has been previously laid out (see {@link Label#layout}).  The label
 * should not be altered after the sprite is created.
 */
public class LabelSprite extends Sprite
{
    /**
     * Constructs a label sprite that renders itself with the specified
     * label.
     */
    public LabelSprite (Label label)
    {
        this(0, 0, label);
    }

    /**
     * Constructs a label sprite with the given initial position that
     * renders itself with the specified label.
     */
    public LabelSprite (int x, int y, Label label)
    {
        super(x, y);

        _label = label;
    }

    /**
     * Returns the label displayed by this sprite.
     */
    public Label getLabel ()
    {
        return _label;
    }

    // documentation inherited
    protected void init (SpriteManager spritemgr)
    {
        super.init(spritemgr);

        // size the bounds to fit our label
        Dimension size = _label.getSize();
        _bounds.width = size.width;
        _bounds.height = size.height;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        _label.render(gfx, _bounds.x, _bounds.y);
    }

    /** The label associated with this sprite. */
    protected Label _label;
}
