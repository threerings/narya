//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.data;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.presents.dobj.DSet;

/**
 * Represents a chat channel.
 */
public abstract class ChatChannel extends SimpleStreamableObject
    implements Comparable<ChatChannel>, DSet.Entry
{
    // from interface Comparable<ChatChannel>
    public abstract int compareTo (ChatChannel other);

    /**
     * Converts this channel into a unique name that can be used as the name of the distributed
     * lock used when resolving the channel.
     */
    public abstract String getLockName ();

    // from interface DSet.Entry
    public Comparable<?> getKey ()
    {
        return this;
    }

    @Override
    public boolean equals (Object other)
    {
        return compareTo((ChatChannel)other) == 0;
    }

    @Override
    public abstract int hashCode ();
}
