//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.peer.data;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.util.Name;

import com.threerings.presents.dobj.DSet;

/**
 * Contains information on a particular client.
 */
public class ClientInfo extends SimpleStreamableObject
    implements DSet.Entry
{
    /** The username used by this client to authenticate. */
    public Name username;

    // documentation inherited from interface DSet.Entry
    public Comparable<?> getKey ()
    {
        return username;
    }
}
