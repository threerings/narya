//
// $Id: DirectionalEdge.java,v 1.2 2001/08/23 23:44:12 shaper Exp $

package com.threerings.nodemap.direction;

import java.awt.*;

import com.threerings.nodemap.*;

/**
 * A directional edge extends the {@link Edge} object to allow
 * associating a direction with the edge and rendering the edge to the
 * screen.  The edge direction must be one of the directional
 * constants detailed in the {@link Directions} object.
 */
public class DirectionalEdge extends Edge
{
    /** The edge direction as a {@link Directions} constant. */
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
}
