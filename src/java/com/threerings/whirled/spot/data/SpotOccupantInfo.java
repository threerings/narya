//
// $Id: SpotOccupantInfo.java,v 1.1 2001/12/14 00:12:32 mdb Exp $

package com.threerings.whirled.spot.data;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.crowd.data.OccupantInfo;

/**
 * The spot services extend the basic occupant info with information on
 * which location within the scene a user occupies.
 */
public class SpotOccupantInfo extends OccupantInfo
{
    /** The id of the location occupied by this user or -1 if they occupy
     * no location. */
    public int locationId;

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeInt(locationId);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        locationId = in.readInt();
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", locId=").append(locationId);
    }
}
