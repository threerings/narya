//
// $Id: SpriteIcon.java,v 1.3 2002/09/13 04:50:31 mdb Exp $

package com.threerings.media.sprite;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * Implements the icon interface, using a {@link Sprite} to render the
 * icon image.
 */
public class SpriteIcon implements Icon
{
    /**
     * Creates a sprite icon that will use the supplied sprite to render
     * itself. This sprite should not be used for anything else while
     * being used in this icon because it will be "moved" when the icon is
     * rendered. The sprite's origin will be set to the bottom center of
     * the label. If this is undesirable, the origin can be offset via
     * {@link #setOriginOffset}.
     */
    public SpriteIcon (Sprite sprite)
    {
        this(sprite, 0);
    }

    /**
     * Creates a sprite icon that will use the supplied sprite to render
     * itself. This sprite should not be used for anything else while
     * being used in this icon because it will be "moved" when the icon is
     * rendered. The sprite's origin will be set to the bottom center of
     * the label. If this is undesirable, the origin can be offset via
     * {@link #setOriginOffset}.
     *
     * @param sprite the sprite to render in this label.
     * @param padding the number of pixels of blank space to put on all
     * four sides of the sprite.
     */
    public SpriteIcon (Sprite sprite, int padding)
    {
        _sprite = sprite;
        _padding = padding;
    }

    /**
     * Origin offsets to use when rendering the icon (useful for tweaking
     * when trying to get sprites that weren't designed to be stuffed into
     * labels to behave appropriately).
     */
    public void setOriginOffset (int offx, int offy)
    {
        _offx = offx;
        _offy = offy;
    }

    // documentation inherited from interface
    public void paintIcon (Component c, Graphics g, int x, int y)
    {
        // move the sprite to a "location" that is in the bottom center of
        // our bounds (plus whatever offsets we have)
        _sprite.setLocation(x + _sprite.getWidth()/2 + _offx + _padding,
                            y + _sprite.getHeight() + _offy + _padding);
        _sprite.paint((Graphics2D)g);
    }
    
    // documentation inherited from interface
    public int getIconWidth ()
    {
        return _sprite.getWidth() + 2*_padding;
    }

    // documentation inherited from interface
    public int getIconHeight ()
    {
        return _sprite.getHeight() + 2*_padding;
    }

    /** The sprite used to render this icon. */
    protected Sprite _sprite;

    /** Used to put a bit of padding around the sprite image. */
    protected int _padding;

    /** Origin offsets to use when rendering the icon. */
    protected int _offx, _offy;
}
