//
// $Id: SpritePanel.java,v 1.4 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast.builder;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

import com.threerings.media.sprite.*;

import com.threerings.cast.Log;
import com.threerings.cast.*;

/**
 * The sprite panel displays a character sprite centered in the panel
 * suitable for user perusal.
 */
public class SpritePanel
    extends AnimatedPanel
    implements BuilderModelListener
{
    /**
     * Constructs the sprite panel.
     */
    public SpritePanel (CharacterManager charmgr, BuilderModel model)
    {
        // save off references
        _charmgr = charmgr;
        _model = model;

        // create managers
        _spritemgr = new SpriteManager();
        _animmgr = new AnimationManager(_spritemgr, this);

        // create a visually pleasing border
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        // listen to the builder model so that we can update the
        // sprite when a new component is selected
        _model.addListener(this);
    }

    // documentation inherited
    protected void render (Graphics g)
    {
        Graphics2D gfx = (Graphics2D)g;

        // clear the background
        gfx.setColor(Color.lightGray);
        Dimension d = getSize();
        gfx.fillRect(0, 0, d.width - 1, d.height - 1);

        if (_sprite == null) {
            // create the sprite if it's not yet extant
            generateSprite();
        }

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

    // documentation inherited
    public void modelChanged (int event)
    {
        if (event == COMPONENT_CHANGED) {
            generateSprite();
        }
    }

    /**
     * Generates a new character sprite for display to reflect the
     * currently selected character components.
     */
    protected void generateSprite ()
    {
        int components[] = _model.getSelectedComponents();
        CharacterDescriptor desc = new CharacterDescriptor(components);
        CharacterSprite sprite = _charmgr.getCharacter(desc);
        setSprite(sprite);
    }

    /**
     * Sets the sprite to be displayed.
     */
    protected void setSprite (CharacterSprite sprite)
    {
        _sprite = sprite;
        _sprite.setOrientation(Sprite.DIR_SOUTHWEST);
        centerSprite();
        repaint();
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

    /** The character manager. */
    protected CharacterManager _charmgr;

    /** The builder model. */
    protected BuilderModel _model;
}
