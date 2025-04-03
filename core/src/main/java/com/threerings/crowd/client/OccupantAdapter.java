//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
