//
// $Id: SpritePanel.java,v 1.13 2002/04/23 01:17:28 mdb Exp $

package com.threerings.cast.builder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.List;

import com.threerings.media.FrameManager;
import com.threerings.media.MediaPanel;

import com.threerings.util.DirectionCodes;

import com.threerings.cast.Log;
import com.threerings.cast.CharacterDescriptor;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.CharacterSprite;
import com.threerings.cast.StandardActions;

/**
 * The sprite panel displays a character sprite centered in the panel
 * suitable for user perusal.
 */
public class SpritePanel extends MediaPanel
    implements DirectionCodes, BuilderModelListener
{
    /**
     * Constructs the sprite panel.
     */
    public SpritePanel (FrameManager framemgr, CharacterManager charmgr,
                        BuilderModel model)
    {
        super(framemgr);

        // save off references
        _charmgr = charmgr;
        _model = model;

        // listen to the builder model so that we can update the
        // sprite when a new component is selected
        _model.addListener(this);
    }

    // documentation inherited
    protected void paintMiddle (Graphics2D gfx, List invalidRects)
    {
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
            _sprite.paint(gfx);
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
        CharacterDescriptor desc = new CharacterDescriptor(components, null);
        CharacterSprite sprite = _charmgr.getCharacter(desc);
        setSprite(sprite);
    }

    /**
     * Sets the sprite to be displayed.
     */
    protected void setSprite (CharacterSprite sprite)
    {
        sprite.setActionSequence(StandardActions.STANDING);
        sprite.setOrientation(WEST);
        _sprite = sprite;
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
    protected CharacterSprite _sprite;

    /** The character manager. */
    protected CharacterManager _charmgr;

    /** The builder model. */
    protected BuilderModel _model;
}
