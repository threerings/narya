//
// $Id: GeomUtil.java,v 1.1 2002/06/25 00:22:44 mdb Exp $

package com.threerings.geom;

import java.awt.Point;

/**
 * General geometry utilites.
 */
public class GeomUtil
{
    /**
     * Computes and returns the dot product of the two vectors.
     *
     * @param v1s the starting point of the first vector.
     * @param v1e the ending point of the first vector.
     * @param v2s the starting point of the second vector.
     * @param v2e the ending point of the second vector.
     */
    public static int dot (Point v1s, Point v1e, Point v2s, Point v2e)
    {
        return ((v1e.x - v1s.x) * (v2e.x - v2s.x) +
                (v1e.y - v1s.y) * (v2e.y - v2s.y));
    }

    /**
     * Computes the point nearest to the specified point <code>p3</code>
     * on the line defined by the two points <code>p1</code> and
     * <code>p2</code>. The computed point is stored into <code>n</code>.
     * <em>Note:</em> <code>p1</code> and <code>p2</code> must not be
     * coincident.
     *
     * @param p1 one point on the line.
     * @param p2 another point on the line (not equal to <code>p1</code>).
     * @param p3 the point to which we wish to be most near.
     * @param n the point on the line defined by <code>p1</code> and
     * <code>p2</code> that is nearest to <code>p</code>.
     *
     * @return the point object supplied via <code>n</code>.
     */
    public static Point nearestToLine (Point p1, Point p2, Point p3, Point n)
    {
        // see http://astronomy.swin.edu.au/~pbourke/geometry/pointline/
        // for a (not very good) explanation of the math
        int Ax = p2.x - p1.x, Ay = p2.y - p1.y;
        float u = (p3.x - p1.x) * Ax + (p3.y - p1.y) * Ay;
        u /= (Ax * Ax + Ay * Ay);
        n.x = p1.x + Math.round(Ax * u);
        n.y = p1.y + Math.round(Ay * u);
        return n;
    }
}
