//
// $Id: DirectionalEdge.java,v 1.7 2004/08/27 02:20:11 mdb Exp $
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

package com.threerings.nodemap.direction;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Stroke;

import com.threerings.nodemap.Edge;
import com.threerings.nodemap.Node;

/**
 * A directional edge extends the {@link Edge} object to allow associating
 * a direction with the edge and rendering the edge to the screen.  The
 * edge direction must be one of the directional constants detailed in the
 * {@link DirectionCodes} class.
 */
public class DirectionalEdge extends Edge
{
    /** The edge direction as a {@link DirectionCodes} constant. */
    public int dir;

    /**
     * Construct a directional edge.
     *
     * @param src the source node.
     * @param dst the destination node.
     * @param dir the edge direction.
     */
    public DirectionalEdge (Node src, Node dst, int dir)
    {
	super(src, dst);
	this.dir = dir;
    }

    // documentation inherited
    public void paint (Graphics g)
    {
        Graphics2D gfx = (Graphics2D)g;
        Stroke ostroke = gfx.getStroke();
        if (_stroke != null) {
            gfx.setStroke(_stroke);
        }
        if (_color != null) {
            gfx.setColor(_color);
        }

	int sx = src.getX(), sy = src.getY();
	int dx = dst.getX(), dy = dst.getY();

	int csx = sx + (src.getWidth() / 2);
	int csy = sy + (src.getHeight() / 2);

	int cdx = dx + (dst.getWidth() / 2);
	int cdy = dy + (dst.getHeight() / 2);

	gfx.drawLine(csx, csy, cdx, cdy);
        gfx.setStroke(ostroke);
    }

    // documentation inherited
    public void toString (StringBuffer buf)
    {
	super.toString(buf);
	buf.append(", dir=").append(dir);
    }
}
