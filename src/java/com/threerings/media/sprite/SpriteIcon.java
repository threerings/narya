//
// $Id: SpriteIcon.java,v 1.4 2002/11/05 03:40:06 mdb Exp $

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

    // documentation inherited from interface
    public void paintIcon (Component c, Graphics g, int x, int y)
    {
        // move the sprite to a "location" that results in its image being
        // in the upper left of the rectangle we desire
        _sprite.setLocation(x + _sprite.getXOffset() + _padding,
                            y + _sprite.getYOffset() + _padding);
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
}
