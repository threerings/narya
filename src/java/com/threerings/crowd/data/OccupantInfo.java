//
// $Id: OccupantInfo.java,v 1.10 2002/10/26 02:40:30 shaper Exp $

package com.threerings.crowd.data;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.presents.dobj.DSet;

/**
 * The occupant info object contains all of the information about an
 * occupant of a place that should be shared with other occupants of the
 * place. These objects are stored in the place object itself and are
 * updated when bodies enter and exit a place.
 *
 * <p> A system that builds upon the Crowd framework can extend this class
 * to include extra information about their occupants. They will need to
 * be sure to return the proper class from {@link
 * com.threerings.crowd.server.PlaceManager#getOccupantInfoClass} and
 * populate their occupant info in {@link
 * com.threerings.crowd.server.PlaceManager#populateOccupantInfo}.
 *
 * <p> Note also that this class implements {@link Cloneable} which means
 * that if derived classes add non-primitive attributes, they are
 * responsible for adding the code to clone those attributes when a clone
 * is requested.
 */
public class OccupantInfo extends SimpleStreamableObject
    implements DSet.Entry, Cloneable
{
    /** Constant value for {@link #status}. */
    public static final byte ACTIVE = 0;

    /** Constant value for {@link #status}. */
    public static final byte IDLE = 1;

    /** Constant value for {@link #status}. */
    public static final byte DISCONNECTED = 2;

    /** The body object id of this occupant (and our entry key). */
    public Integer bodyOid;

    /** The username of this occupant. */
    public String username;

    /** The status of this occupant. */
    public byte status = ACTIVE;

    /** Access to the body object id as an int. */
    public int getBodyOid ()
    {
        return bodyOid.intValue();
    }

    // documentation inherited
    public Comparable getKey ()
    {
        return bodyOid;
    }

    /**
     * Generates a cloned copy of this instance.
     */
    public Object clone ()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("WTF? " + cnse);
        }
    }
}
