//
// $Id: OccupantInfo.java,v 1.7 2002/04/17 22:19:40 mdb Exp $

package com.threerings.crowd.data;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.io.SimpleStreamableObject;

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
 *
 * <p> Note also that this class extends {@link SimpleStreamableObject}
 * which means that public data members will automatically be serialized,
 * but if any non-public or data members are added, code must be added to
 * serialize them when this object is streamed.
 */
public class OccupantInfo extends SimpleStreamableObject
    implements DSet.Entry, Cloneable
{
    /** The body object id of this occupant (and our entry key). */
    public Integer bodyOid;

    /** The username of this occupant. */
    public String username;

    /** Access to the body object id as an int. */
    public int getBodyOid ()
    {
        return bodyOid.intValue();
    }

    // documentation inherited
    public Object getKey ()
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
