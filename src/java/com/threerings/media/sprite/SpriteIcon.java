//
// $Id: SpriteIcon.java,v 1.2 2002/05/16 03:00:29 mdb Exp $

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
        _sprite = sprite;
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
        _sprite.setLocation(x + _sprite.getWidth()/2 + _offx,
                            y + _sprite.getHeight() + _offy);
        _sprite.paint((Graphics2D)g);
    }
    
    // documentation inherited from interface
    public int getIconWidth ()
    {
        return _sprite.getWidth();
    }

    // documentation inherited from interface
    public int getIconHeight ()
    {
        return _sprite.getHeight();
    }

    /** The sprite used to render this icon. */
    protected Sprite _sprite;

    /** Origin offsets to use when rendering the icon. */
    protected int _offx, _offy;
}
