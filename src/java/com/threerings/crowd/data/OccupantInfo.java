//
// $Id: OccupantInfo.java,v 1.5 2001/12/15 04:20:00 mdb Exp $

package com.threerings.crowd.data;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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
public class OccupantInfo
    implements DSet.Element, Cloneable
{
    /** The body object id of this occupant (and our element key). */
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

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeInt(bodyOid.intValue());
        out.writeUTF(username);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        bodyOid = new Integer(in.readInt());
        username = in.readUTF();
    }

    /**
     * Called to generate a string representation of this occupant info
     * object. This calls the overridable {@link #toString(StringBuffer)}
     * to generate that representation in a derived class friendly manner.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * Derived classes should override this and append their extra
     * occupant info to the buffer. They should of course not to forget to
     * call super.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("boid=").append(bodyOid);
        buf.append(", username=").append(username);
    }
}
