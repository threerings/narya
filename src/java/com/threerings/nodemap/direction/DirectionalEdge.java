//
// $Id: DirectionalEdge.java,v 1.4 2001/12/17 03:34:04 mdb Exp $

package com.threerings.nodemap.direction;

import java.awt.Color;
import java.awt.Graphics;

import com.threerings.util.DirectionCodes;

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

    /**
     * Render the edge to the given graphics context.
     *
     * @param g the graphics context.
     */
    public void paint (Graphics g)
    {
	g.setColor(Color.black);

	int sx = src.getX(), sy = src.getY();
	int dx = dst.getX(), dy = dst.getY();

	int csx = sx + (src.getWidth() / 2);
	int csy = sy + (src.getHeight() / 2);

	int cdx = dx + (dst.getWidth() / 2);
	int cdy = dy + (dst.getHeight() / 2);

	g.drawLine(csx, csy, cdx, cdy);
    }

    public void toString (StringBuffer buf)
    {
	super.toString(buf);
	buf.append(", dir=").append(dir);
    }
}
