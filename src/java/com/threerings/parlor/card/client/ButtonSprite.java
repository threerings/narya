//
// $Id: ButtonSprite.java,v 1.3 2004/11/05 02:07:19 andrzej Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.parlor.card.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;

import com.samskivert.swing.Label;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.sprite.Sprite;

import com.threerings.parlor.card.Log;

/**
 * A sprite that acts as a button.
 */
public class ButtonSprite extends Sprite
{
    /** The normal, square button style. */
    public static final int NORMAL = 0;
    
    /** The rounded button style. */
    public static final int ROUNDED = 1;
    
    /**
     * Constructs a button sprite.
     *
     * @param label the label to render on the button
     * @param style the style of button to render (NORMAL or ROUNDED)
     * @param backgroundColor the background color of the button
     * @param alternateColor the alternate (outline) color
     * @param actionCommand the button's command
     * @param commandArgument the button's command argument
     */
    public ButtonSprite (Label label, int style, Color backgroundColor,
        Color alternateColor, String actionCommand, Object commandArgument)
    {
        _label = label;
        _style = style;
        _backgroundColor = backgroundColor;
        _alternateColor = alternateColor;
        _actionCommand = actionCommand;
        _commandArgument = commandArgument;
    }

    /**
     * Constructs a button sprite.
     *
     * @param label the label to render on the button
     * @param style the style of button to render (NORMAL or ROUNDED)
     * @param arcWidth the width of the corner arcs for rounded buttons
     * @param arcHeight the height of the corner arcs for rounded buttons
     * @param backgroundColor the background color of the button
     * @param alternateColor the alternate (outline) color
     * @param actionCommand the button's command
     * @param commandArgument the button's command argument
     */
    public ButtonSprite (Label label, int style, int arcWidth, int arcHeight,
        Color backgroundColor, Color alternateColor, String actionCommand, 
        Object commandArgument)
    {
        _label = label;
        _style = style;
        _arcWidth = arcWidth;
        _arcHeight = arcHeight;
        _backgroundColor = backgroundColor;
        _alternateColor = alternateColor;
        _actionCommand = actionCommand;
        _commandArgument = commandArgument;
    }
    
    /**
     * Returns a reference to the label displayed by this sprite.
     */
    public Label getLabel ()
    {
        return _label;
    }

    /**
     * Updates this sprite's bounds after a change to the label.
     */
    public void updateBounds ()
    {
        // size the bounds to fit our label
        Dimension size = _label.getSize();
        _bounds.width = size.width + PADDING*2 + 
            (_style == ROUNDED ? _arcWidth : 0);
        _bounds.height = size.height + PADDING*2;
    }
    
    /**
     * Sets the style of this button.
     */
    public void setStyle (int style)
    {
        _style = style;
        updateBounds();
    }
    
    /**
     * Returns the style of this button.
     */
    public int getStyle ()
    {
        return _style;
    }
    
    /**
     * Sets the arc width for rounded buttons.
     */
    public void setArcWidth (int arcWidth) 
    {
        _arcWidth = arcWidth;
        updateBounds();
    }
    
    /**
     * Returns the arc width for rounded buttons.
     */
    public int getArcWidth ()
    {
        return _arcWidth;
    }
    
    /**
     * Sets the arc height for rounded buttons.
     */
    public void setArcHeight (int arcHeight) 
    {
        _arcHeight = arcHeight;
        updateBounds();
    }
    
    /**
     * Returns the arc height for rounded buttons.
     */
    public int getArcHeight ()
    {
        return _arcHeight;
    }
    
    /**
     * Sets the background color of this button.
     */
    public void setBackgroundColor (Color backgroundColor)
    {
        _backgroundColor = backgroundColor;
    }
    
    /**
     * Returns the background color of this button.
     */
    public Color getBackgroundColor ()
    {
        return _backgroundColor;
    }
    
    /**
     * Sets the action command generated by this button.
     */
    public void setActionCommand (String actionCommand)
    {
        _actionCommand = actionCommand;
    }
    
    /**
     * Returns the action command generated by this button.
     */
    public String getActionCommand ()
    {
        return _actionCommand;
    }
    
    /**
     * Sets the command argument generated by this button.
     */
    public void setCommandArgument (Object commandArgument)
    {
        _commandArgument = commandArgument;
    }
    
    /**
     * Returns the command argument generated by this button.
     */
    public Object getCommandArgument ()
    {
        return _commandArgument;
    }
    
    /**
     * Sets whether or not this button is enabled.
     */
    public void setEnabled (boolean enabled)
    {
        if (_enabled != enabled) {
            _enabled = enabled;
            invalidate();
        }
    }
    
    /**
     * Checks whether or not this button is enabled.
     */
    public boolean isEnabled ()
    {
        return _enabled;
    }
    
    /**
     * Sets whether or not this button appears pressed
     * (does not fire an event).
     */
    public void setPressed (boolean pressed)
    {
        if (_pressed != pressed) {
            _pressed = pressed;
            invalidate();
        }
    }
    
    /** 
     * Checks whether or not this button appears pressed.
     */
    public boolean isPressed ()
    {
        return _pressed;
    }
    
    // documentation inherited
    protected void init ()
    {
        super.init();

        updateBounds();
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        Color baseTextColor = _label.getTextColor(),
            baseAlternateColor = _label.getAlternateColor();
        
        if (!_enabled) {
            _label.setTextColor(baseTextColor.darker());
            _label.setAlternateColor(baseAlternateColor.darker());
        }
        
        switch (_style) {
            case NORMAL:
                gfx.setColor(_enabled ? _backgroundColor : _backgroundColor.darker());
                gfx.fill3DRect(_bounds.x, _bounds.y, _bounds.width, 
                    _bounds.height, !_pressed);
                _label.render(gfx, _bounds.x + (_pressed ? PADDING : PADDING - 1),
                    _bounds.y + (_pressed ? PADDING : PADDING - 1));
                break;
            case ROUNDED:
                Object aaState = SwingUtil.activateAntiAliasing(gfx);
                // draw outline
                gfx.setColor(_alternateColor);
                gfx.fillRoundRect(_bounds.x, _bounds.y, _bounds.width, _bounds.height, 
                    _arcWidth, _arcHeight);
                // draw foreground
                gfx.setColor(_enabled ? _backgroundColor : _backgroundColor.darker());
                int innerBoundsX = _bounds.x+1, innerBoundsY = _bounds.y+1, 
                    innerBoundsWidth = _bounds.width-2, innerBoundsHeight = _bounds.height-2,
                    innerBoundsArcWidth = _arcWidth-2, innerBoundsArcHeight = _arcHeight-2;
                gfx.fillRoundRect(innerBoundsX, innerBoundsY, innerBoundsWidth, innerBoundsHeight,
                    innerBoundsArcWidth, innerBoundsArcHeight);
                Color brighter = _enabled ? _backgroundColor.brighter() : _backgroundColor,
                    darker = _enabled ? _backgroundColor.darker() : _backgroundColor.darker().darker();
                // draw the upper left/lower right corners (always dark)
                gfx.setColor(darker);
                gfx.drawArc(innerBoundsX, innerBoundsY, innerBoundsArcWidth, innerBoundsArcHeight, 90, 90);
                gfx.drawArc(innerBoundsX + innerBoundsWidth - innerBoundsArcWidth - 1,
                    innerBoundsY + innerBoundsHeight - innerBoundsArcHeight - 1,
                    innerBoundsArcWidth, innerBoundsArcHeight, 270, 90);
                // draw the upper right (dark when pressed)
                gfx.setColor(_pressed ? darker : brighter);
                gfx.drawLine(innerBoundsX + innerBoundsArcWidth/2, innerBoundsY, 
                    innerBoundsX + innerBoundsWidth - innerBoundsArcWidth/2, innerBoundsY);
                gfx.drawArc(innerBoundsX + innerBoundsWidth - innerBoundsArcWidth - 1, innerBoundsY,
                    innerBoundsArcWidth, innerBoundsArcHeight, 0, 90);
                gfx.drawLine(innerBoundsX + innerBoundsWidth - 1, innerBoundsY + innerBoundsArcHeight/2,
                    innerBoundsX + innerBoundsWidth - 1, innerBoundsY + innerBoundsHeight - innerBoundsArcHeight/2);
                // draw the lower left (light when pressed)
                gfx.setColor(_pressed ? brighter : darker);
                gfx.drawLine(innerBoundsX, innerBoundsY + innerBoundsArcHeight/2, innerBoundsX,
                    innerBoundsY + innerBoundsHeight - innerBoundsArcHeight/2);
                gfx.drawArc(innerBoundsX, innerBoundsY + innerBoundsHeight - innerBoundsArcHeight - 1,
                    innerBoundsArcWidth, innerBoundsArcHeight, 180, 90);
                gfx.drawLine(innerBoundsX + innerBoundsArcWidth/2, innerBoundsY + innerBoundsHeight - 1, 
                    innerBoundsX + innerBoundsWidth - innerBoundsArcWidth/2, 
                    innerBoundsY + innerBoundsHeight - 1);
                SwingUtil.restoreAntiAliasing(gfx, aaState);
                _label.render(gfx, _bounds.x + PADDING + _arcWidth/2 - (_pressed ? 2 : 1), 
                    _bounds.y + PADDING + (_pressed ? 1 : 0));
                break;
        }
        
        if (!_enabled) {
            _label.setTextColor(baseTextColor);
            _label.setAlternateColor(baseAlternateColor);
        }
    }

    /** The number of pixels to add between the text and the border. */
    protected static final int PADDING = 2;
    
    /** The label associated with this sprite. */
    protected Label _label;
    
    /** The button style. */
    protected int _style;
    
    /** The width of the corner arcs for rounded rectangle buttons. */
    protected int _arcWidth;
    
    /** The height of the corner arcs for rounded rectangle buttons. */
    protected int _arcHeight;
    
    /** The action command generated by this button. */
    protected String _actionCommand;
    
    /** The command argument generated by this button. */
    protected Object _commandArgument;
    
    /** The background color of this sprite. */
    protected Color _backgroundColor;
    
    /** The alternate (outline) color of this sprite. */
    protected Color _alternateColor;
    
    /** Whether or not the button is currently enabled. */
    protected boolean _enabled = true;
    
    /** Whether or not the button is currently pressed. */
    protected boolean _pressed;
}
