//
// $Id: DirectionalEdge.java,v 1.1 2001/08/20 22:56:55 shaper Exp $

package com.threerings.nodemap.direction;

import java.awt.*;

import com.threerings.nodemap.*;

public class DirectionalEdge extends Edge
{
    /** The edge direction as a {@link Directions} constant. */
    public int dir;

    public DirectionalEdge (Node src, Node dst, int dir)
    {
	super(src, dst);
	this.dir = dir;
    }

    public void paint (Graphics g)
    {
	g.setColor(Color.black);

	Point spos = src.getPosition(), dpos = dst.getPosition();

	int csx = spos.x + (src.getWidth() / 2);
	int csy = spos.y + (src.getHeight() / 2);

	int cdx = dpos.x + (dst.getWidth() / 2);
	int cdy = dpos.y + (dst.getHeight() / 2);

	g.drawLine(csx, csy, cdx, cdy);
    }
}
