//
// $Id: TilePathNode.java,v 1.1 2001/10/24 00:55:08 shaper Exp $

package com.threerings.miso.scene;

import com.threerings.media.sprite.PathNode;

/**
 * The tile path nodes extends the path node class to allow
 * associating tile coordinates with a node in a path.
 */
public class TilePathNode extends PathNode
{
    /**
     * Constructs a tile path node.
     */
    public TilePathNode (int tilex, int tiley, int x, int y, int dir)
    {
        super(x, y, dir);

        _tilex = tilex;
        _tiley = tiley;
    }

    /**
     * Returns the node's x-axis tile coordinates.
     */
    public int getTileX ()
    {
        return _tilex;
    }

    /**
     * Returns the node's y-axis tile coordinates.
     */
    public int getTileY ()
    {
        return _tiley;
    }

    // documentation inherited
    public void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", tilex=").append(_tilex);
        buf.append(", tiley=").append(_tiley);
    }

    /** The path node tile coordinates. */
    protected int _tilex, _tiley;
}
