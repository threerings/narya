//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.media.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;

import com.threerings.media.Log;

/**
 * Draws a mirage combined with an arbitrary AffineTransform.
 */
public class TransformedMirage
    implements Mirage
{
    /**
     * Constructor.
     */
    public TransformedMirage (Mirage base, AffineTransform transform)
    {
        _base = base;

        // clone the transform so that it doesn't get changed on us.
        _transform = (AffineTransform) transform.clone();
        computeTransformedBounds();
    }

    // documentation inherited from interface Mirage
    public void paint (Graphics2D gfx, int x, int y)
    {
        AffineTransform otrans = gfx.getTransform();
        gfx.translate(x - _bounds.x, y - _bounds.y);
        gfx.transform(_transform);
        _base.paint(gfx, 0, 0);
        gfx.setTransform(otrans);
    }

    // documentation inherited from interface Mirage
    public int getWidth ()
    {
        return _bounds.width;
    }

    // documentation inherited from interface Mirage
    public int getHeight ()
    {
        return _bounds.height;
    }

    // documentation inherited from interface Mirage
    public boolean hitTest (int x, int y)
    {
        Point p = new Point(x, y);
        try {
            _transform.createInverse().transform(p, p);
            return _base.hitTest(p.x, p.y);

        } catch (NoninvertibleTransformException nte) {
            // grumble, grumble
            // TODO: log something?
            return ImageUtil.hitTest(getSnapshot(), x, y);
        }
    }

    // documentation inherited from interface Mirage
    public BufferedImage getSnapshot ()
    {
        BufferedImage baseSnap = _base.getSnapshot();
        BufferedImage img = new BufferedImage(_bounds.width, _bounds.height,
            baseSnap.getType());
        Graphics2D gfx = (Graphics2D) img.getGraphics();
        try {
            gfx.translate(-_bounds.x, -_bounds.y);
            gfx.transform(_transform);
            gfx.drawImage(baseSnap, 0, 0, null);
        } finally {
            gfx.dispose();
        }
        return img;
    }

    // documentation inherited from interface Mirage
    public long getEstimatedMemoryUsage ()
    {
        return _base.getEstimatedMemoryUsage();
    }

    /**
     * Compute the bounds of the base Mirage after it has been
     * transformed.
     */
    protected void computeTransformedBounds ()
    {
        int w = _base.getWidth();
        int h = _base.getHeight();
        Point[] points = new Point[] {
            new Point(0, 0), new Point(w, 0), new Point(0, h), new Point(w, h) };
        _transform.transform(points, 0, points, 0, 4);
        int minX, minY, maxX, maxY;
        minX = minY = Integer.MAX_VALUE;
        maxX = maxY = Integer.MIN_VALUE;
        for (int ii=0; ii < 4; ii++) {
            minX = Math.min(minX, points[ii].x);
            maxX = Math.max(maxX, points[ii].x);
            minY = Math.min(minY, points[ii].y);
            maxY = Math.max(maxY, points[ii].y);
        }

        _bounds = new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /** The base mirage. */
    protected Mirage _base;

    /** Our transformed bounds. */
    protected Rectangle _bounds;

    /** The transform we apply when painting the base mirage. */
    protected AffineTransform _transform;
}
