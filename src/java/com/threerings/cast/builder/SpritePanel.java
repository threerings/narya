//
// $Id: SpritePanel.java,v 1.1 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast.builder;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

import com.threerings.media.sprite.*;

import com.threerings.cast.Log;
import com.threerings.cast.CharacterSprite;

/**
 * The sprite panel displays a character sprite centered in the panel
 * suitable for user perusal.
 */
public class SpritePanel extends AnimatedPanel
{
    /**
     * Constructs the sprite panel.
     */
    public SpritePanel ()
    {
        // create and save off references to our managers
        _spritemgr = new SpriteManager();
        _animmgr = new AnimationManager(_spritemgr, this);

        // create a visually pleasing border
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    /**
     * Sets the sprite to be displayed.
     */
    public void setSprite (CharacterSprite sprite)
    {
        _sprite = sprite;
        _sprite.setOrientation(Sprite.DIR_SOUTHWEST);
        centerSprite();
        repaint();
    }

    // documentation inherited
    protected void render (Graphics g)
    {
        Graphics2D gfx = (Graphics2D)g;

        // clear the background
        gfx.setColor(Color.lightGray);
        Dimension d = getSize();
        gfx.fillRect(0, 0, d.width - 1, d.height - 1);

        if (_sprite != null) {
            // render the sprite
            _sprite.paint((Graphics2D)g);
        }
    }

    // documentation inherited
    public void setBounds (Rectangle r)
    {
        super.setBounds(r);
        centerSprite();
    }

    /**
     * Sets the sprite's location to render it centered within the panel.
     */
    protected void centerSprite ()
    {
        if (_sprite != null) {
            Dimension d = getSize();
            int swid = _sprite.getWidth(), shei = _sprite.getHeight();
            int x = d.width / 2, y = (d.height + shei) / 2;
            _sprite.setLocation(x, y);
        }
    }

    /** The sprite displayed by the panel. */
    protected Sprite _sprite;

    /** The animation manager. */
    protected AnimationManager _animmgr;

    /** The sprite manager. */
    protected SpriteManager _spritemgr;
}
