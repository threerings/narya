//
// $Id: SceneLocation.java,v 1.1 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.spot.data;

import com.threerings.presents.dobj.DSet;

/**
 * Extends {@link Location} with the data and functionality needed to
 * represent a particular user's location in a scene.
 */
public class SceneLocation extends Location
    implements DSet.Entry
{
    /** The oid of the body that occupies this location. */
    public int bodyOid;

    /**
     * Creates a scene location with the specified information.
     */
    public SceneLocation (int x, int y, byte orient, int bodyOid)
    {
        super(x, y, orient);
        this.bodyOid = bodyOid;
    }

    /**
     * Creates a scene location with the specified information.
     */
    public SceneLocation (Location loc, int bodyOid)
    {
        super(loc.x, loc.y, loc.orient);
        this.bodyOid = bodyOid;
    }

    /**
     * Creates a blank instance suitable for unserialization.
     */
    public SceneLocation ()
    {
    }

    // documentation inherited
    public Comparable getKey ()
    {
        if (_key == null) {
            _key = new Integer(bodyOid);
        }
        return _key;
    }

    /** Used for {@link #geyKey}. */
    protected transient Integer _key;
}
