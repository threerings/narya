//
// $Id: LabelSprite.java,v 1.5 2004/02/25 14:43:17 mdb Exp $

package com.threerings.media.sprite;

import java.awt.Dimension;
import java.awt.Graphics2D;

import com.samskivert.swing.Label;

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
    protected void init ()
    {
        super.init();

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
