//
// $Id: ZoneAdapter.java,v 1.1 2002/11/12 19:53:35 shaper Exp $

package com.threerings.whirled.zone.client;

import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * The zone adapter makes life easier for a class that really only cares
 * about one or two of the zone observer callbacks and doesn't want to
 * provide empty implementations of the others. One can either extend zone
 * adapter, or create an anonymous instance that overrides the desired
 * callback(s).
 *
 * @see ZoneObserver
 */
public class ZoneAdapter implements ZoneObserver
{
    // documentation inherited from interface
    public void zoneWillChange (int zoneId)
    {
    }

    // documentation inherited from interface
    public void zoneDidChange (ZoneSummary summary)
    {
    }

    // documentation inherited from interface
    public void zoneChangeFailed (String reason)
    {
    }
}
