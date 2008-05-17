//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.net;

import java.util.HashMap;

import com.samskivert.util.HashIntMap;

/**
 * Message transport parameters.  These include the type of transport and the channel (used to
 * define independent streams for ordered transport), and may eventually include message
 * priority, etc.
 */
public class Transport
{
    /** The unreliable/unordered mode of transport. */
    public static final Transport UNRELIABLE_UNORDERED = getInstance(
        TransportType.UNRELIABLE_UNORDERED);

    /** The unreliable/ordered mode on the default channel. */
    public static final Transport UNRELIABLE_ORDERED = getInstance(
        TransportType.UNRELIABLE_ORDERED, 0);

    /** The reliable/unordered mode. */
    public static final Transport RELIABLE_UNORDERED = getInstance(
        TransportType.RELIABLE_UNORDERED);

    /** The reliable/ordered mode on the default channel. */
    public static final Transport RELIABLE_ORDERED = getInstance(
        TransportType.RELIABLE_ORDERED, 0);

    /** The default mode of transport. */
    public static final Transport DEFAULT = RELIABLE_ORDERED;

    /**
     * Returns the shared instance with the specified parameters.
     */
    public static Transport getInstance (TransportType type)
    {
        return getInstance(type, 0);
    }

    /**
     * Returns the shared instance with the specified parameters.
     */
    public static Transport getInstance (TransportType type, int channel)
    {
        // were there more parameters in transport objects, it would be better to have a single map
        // of instances and use Transport objects as keys (as in examples of the flyweight
        // pattern).  however, doing it this way avoids the need to create a new object on lookup
        if (_unordered == null) {
            _unordered = new HashMap<TransportType, Transport>();
            _ordered = new HashMap<TransportType, HashIntMap<Transport>>();
        }

        // for unordered transport, we map on the type alone
        if (!type.isOrdered()) {
            Transport instance = _unordered.get(type);
            if (instance == null) {
                _unordered.put(type, instance = new Transport(type));
            }
            return instance;
        }

        // for ordered transport, we map on the type and channel
        HashIntMap<Transport> instances = _ordered.get(type);
        if (instances == null) {
            _ordered.put(type, instances = new HashIntMap<Transport>());
        }
        Transport instance = instances.get(channel);
        if (instance == null) {
            instances.put(channel, instance = new Transport(type, channel));
        }
        return instance;
    }

    /**
     * Returns the type of transport.
     */
    public TransportType getType ()
    {
        return _type;
    }

    /**
     * Returns the transport channel.
     */
    public int getChannel ()
    {
        return _channel;
    }

    /**
     * Checks whether this transport guarantees that messages will be delivered.
     */
    public boolean isReliable ()
    {
        return _type.isReliable();
    }

    /**
     * Checks whether this transport guarantees that messages will be received in the order in
     * which they were sent, if they are received at all.
     */
    public boolean isOrdered ()
    {
        return _type.isOrdered();
    }

    /**
     * Returns a transport that satisfies the requirements of this and the specified other
     * transport.
     */
    public Transport combine (Transport other)
    {
        // if the channels are different, we fall back to the default channel
        return getInstance(
            _type.combine(other._type),
            (_channel == other._channel) ? _channel : 0);
    }

    @Override // documentation inherited
    public int hashCode ()
    {
        return 31*_type.hashCode() + _channel;
    }

    @Override // documentation inherited
    public boolean equals (Object other)
    {
        Transport otrans;
        return other instanceof Transport && (otrans = (Transport)other)._type == _type &&
            otrans._channel == _channel;
    }

    @Override // documentation inherited
    public String toString ()
    {
        return "[type=" + _type + ", channel=" + _channel + "]";
    }

    protected Transport (TransportType type)
    {
        this(type, 0);
    }

    protected Transport (TransportType type, int channel)
    {
        _type = type;
        _channel = channel;
    }

    /** The type of transport. */
    protected TransportType _type;

    /** The transport channel. */
    protected int _channel;

    /** Unordered instances mapped by type (would use {@link java.util.EnumMap}, but it doesn't
     * work with Retroweaver). */
    protected static HashMap<TransportType, Transport> _unordered;

    /** Ordered instances mapped by type and channel. */
    protected static HashMap<TransportType, HashIntMap<Transport>> _ordered;
}
