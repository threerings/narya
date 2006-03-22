//
// $Id: CompositedMultiFrameImage.java 3310 2005-01-24 23:08:21Z mdb $
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

package com.threerings.cast;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Transparency;

import com.threerings.media.image.ImageManager;
import com.threerings.media.image.Mirage;
import com.threerings.media.image.VolatileMirage;

import com.threerings.cast.CompositedActionFrames.ComponentFrames;

/**
 * Used to composite action frames with mask frames.
 */
public class CompositedMaskedImage extends CompositedMultiFrameImage
{
    public CompositedMaskedImage (
        ImageManager imgr, ComponentFrames[] sources, String action,
        int orient)
    {
        super(imgr, sources, action, orient);
    }
    
    // documentation inherited from interface
    public int getWidth (int index) {
        return _sources[0].frames.getFrames(_orient).getWidth(index);
    }

    // documentation inherited from interface
    public int getHeight (int index) {
        return _sources[0].frames.getFrames(_orient).getHeight(index);
    }

    public int getXOrigin (int index) {
        return _sources[0].frames.getXOrigin(_orient, index);
    }

    public int getYOrigin (int index) {
        return _sources[0].frames.getYOrigin(_orient, index);
    }
    
    // documentation inherited from interface
    public void paintFrame (Graphics2D g, int index, int x, int y) {
        _images[index].paint(g, x + _images[index].getX(),
                             y + _images[index].getY());
    }

    // documentation inherited from interface
    public boolean hitTest (int index, int x, int y) {
        return _images[index].hitTest(x + _images[index].getX(),
                                      y + _images[index].getY());
    }

    // documentation inherited from interface TrimmedMultiFrameImage
    public void getTrimmedBounds (int index, Rectangle bounds) {
        bounds.setBounds(_images[index].getX(), _images[index].getY(),
                         _images[index].getWidth(), _images[index].getHeight());
    }

    // documentation inherited
    protected CompositedMirage createCompositedMirage (int index)
    {
        return new MaskedMirage(index);      
    }
    
    /**
     * Combines the image in the first source with the mask in the second. */
    protected class MaskedMirage extends CompositedMirage
    {
        public MaskedMirage (int index)
        {
            super(index);
        }
        
        // documentation inherited
        protected Rectangle combineBounds (Rectangle bounds, Rectangle tbounds)
        {
            if (bounds.width == 0 && bounds.height == 0) {
                bounds.setBounds(tbounds);
            } else {
                bounds = bounds.intersection(tbounds);
            }
            return bounds;
        }
        
        // documentation inherited
        protected void refreshVolatileImage ()
        {
            Graphics2D g = (Graphics2D)_image.getGraphics();
            try {
                TrimmedMultiFrameImage source =
                    _sources[0].frames.getFrames(_orient),
                    mask = _sources[1].frames.getFrames(_orient);
                source.paintFrame(g, _index, -_bounds.x, -_bounds.y);
                g.setComposite(AlphaComposite.DstIn);
                mask.paintFrame(g, _index, -_bounds.x, -_bounds.y);
                
            } finally {
                // clean up after ourselves
                if (g != null) {
                    g.dispose();
                }
            }
        }
    }
};
