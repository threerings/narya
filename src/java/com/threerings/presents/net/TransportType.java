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

/**
 * The available types of transport.
 */
public enum TransportType
{
    /**
     * Messages are neither guaranteed to arrive nor, if they do arrive, to arrive in order
     * and without duplicates.  Functionally identical to UDP.
     */
    UNRELIABLE_UNORDERED(false, false) {
        public TransportType combine (TransportType other) {
            return other; // we defer to all
        }
    },

    /**
     * Messages are not guaranteed to arrive, but if they do arrive, then they will arrive in
     * order and without duplicates.  In other words, out-of-order packets will be dropped.
     */
    UNRELIABLE_ORDERED(false, true) {
        public TransportType combine (TransportType other) {
            return other.isReliable() ? RELIABLE_ORDERED : this;
        }
    },

    /**
     * Messages are guaranteed to arrive eventually, but they are not guaranteed to arrive in
     * order.
     */
    RELIABLE_UNORDERED(true, false) {
        public TransportType combine (TransportType other) {
            return other.isOrdered() ? RELIABLE_ORDERED : this;
        }
    },

    /**
     * Messages are guaranteed to arrive, and will arrive in the order in which they are sent.
     * Functionally identical to TCP.
     */
    RELIABLE_ORDERED(true, true) {
        public TransportType combine (TransportType other) {
            return this; // we override all
        }
    };

    /**
     * Checks whether this transport type guarantees that messages will be delivered.
     */
    public boolean isReliable ()
    {
        return _reliable;
    }

    /**
     * Checks whether this transport type guarantees that messages will be received in the
     * order in which they were sent, if they are received at all.
     */
    public boolean isOrdered ()
    {
        return _ordered;
    }

    /**
     * Returns a transport type that combines the requirements of this type with those of the
     * specified other type.
     */
    public abstract TransportType combine (TransportType other);

    TransportType (boolean reliable, boolean ordered)
    {
        _reliable = reliable;
        _ordered = ordered;
    }

    protected boolean _reliable, _ordered;
}
