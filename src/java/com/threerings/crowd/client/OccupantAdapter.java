//
// $Id: OccupantAdapter.java,v 1.1 2002/10/27 02:03:40 shaper Exp $

package com.threerings.crowd.client;

import com.threerings.crowd.data.OccupantInfo;

/**
 * The occupant adapter makes life easier for occupant observer classes
 * that only care about one or two of the occupant observer
 * callbacks. They can either extend occupant adapter or create an
 * anonymous class that extends it and overrides just the callbacks they
 * care about.
 */
public class OccupantAdapter implements OccupantObserver
{
    // documentation inherited from interface
    public void occupantEntered (OccupantInfo info)
    {
    }

    // documentation inherited from interface
    public void occupantLeft (OccupantInfo info)
    {
    }

    // documentation inherited from interface
    public void occupantUpdated (OccupantInfo oinfo, OccupantInfo info)
    {
    }
}
