//
// $Id$
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

package com.threerings.media.sprite;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import java.awt.image.*;

import com.threerings.media.image.Mirage;

import com.threerings.media.util.MultiFrameImage;

/**
 * An image sprite that uses AWT's rotation methods to render itself in
 * different orientations.
 */
public class OrientableImageSprite extends ImageSprite
{
    /**
     * Creates a new orientable image sprite.
     */
    public OrientableImageSprite ()
    {}
    
    /**
     * Creates a new orientable image sprite.
     *
     * @param image the image to render
     */
    public OrientableImageSprite (Mirage image)
    {
        super(image);
    }
    
    /**
     * Creates a new orientable image sprite.
     *
     * @param frames the frames to render
     */
    public OrientableImageSprite (MultiFrameImage frames)
    {
        super(frames);
    }
    
    /**
     * Computes and returns the rotation transform for this
     * sprite.
     *
     * @return the newly computed rotation transform
     */ 
    private AffineTransform getRotationTransform ()
    {
        double theta;
        
        switch (_orient) {
            case NORTH:
            default:
                theta = 0.0;
                break;
            
            case SOUTH:
                theta = Math.PI;
                break;
                    
            case EAST:
                theta = Math.PI*0.5;
                break;
                
            case WEST:
                theta = -Math.PI*0.5;
                break;
                
            case NORTHEAST:
                theta = Math.PI*0.25;
                break;
            
            case NORTHWEST:
                theta = -Math.PI*0.25;
                break;
                
            case SOUTHEAST:
                theta = Math.PI*0.75;
                break;
                
            case SOUTHWEST:
                theta = -Math.PI*0.75;
                break;
                
            case NORTHNORTHEAST:
                theta = -Math.PI*0.125;
                break;
                
            case NORTHNORTHWEST:
                theta = Math.PI*0.125;
                break;
                
            case SOUTHSOUTHEAST:
                theta = -Math.PI*0.875;
                break;
                
            case SOUTHSOUTHWEST:
                theta = Math.PI*0.875;
                break;
                
            case EASTNORTHEAST:
                theta = -Math.PI*0.375;
                break;
                
            case EASTSOUTHEAST:
                theta = -Math.PI*0.625;
                break;
                
            case WESTNORTHWEST:
                theta = Math.PI*0.375;
                break;
                
            case WESTSOUTHWEST:
                theta = Math.PI*0.625;
                break;
        }
        
        return AffineTransform.getRotateInstance(
            theta,
            (_ox - _oxoff) + _frames.getWidth(_frameIdx)/2,
            (_oy - _oyoff) + _frames.getHeight(_frameIdx)/2
        );
    }
    
    // Documentation inherited.
    protected void accomodateFrame (int frameIdx, int width, int height)
    {
        Area area = new Area(
            new Rectangle(
                (_ox - _oxoff),
                (_oy - _oyoff),
                width,
                height
            )
        );
        
        area.transform(getRotationTransform());
        
        _bounds = area.getBounds();
    }
    
    // Documentation inherited.
    public void setOrientation (int orient)
    {
        super.setOrientation(orient);
        
        layout();
    }
    
    // Documentation inherited.
    public void paint (Graphics2D graphics)
    {
        AffineTransform at = graphics.getTransform();
        
        graphics.transform(getRotationTransform());
        
        if (_frames != null) {
            _frames.paintFrame(
                graphics, 
                _frameIdx, 
                _ox - _oxoff, 
                _oy - _oyoff
            );
        }
        else {
            super.paint(graphics);
        }
        
        graphics.setTransform(at);
    }
}
