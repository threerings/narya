//
// $Id: DirectionalEdge.java,v 1.5 2002/06/15 02:13:11 shaper Exp $

package com.threerings.nodemap.direction;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Stroke;

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
