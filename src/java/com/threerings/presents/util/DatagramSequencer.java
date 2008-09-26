//
// $Id: AuthRequest.java 4641 2007-03-31 02:35:49Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.threerings.io.UnreliableObjectInputStream;
import com.threerings.io.UnreliableObjectOutputStream;

import com.threerings.presents.net.Message;
import com.threerings.presents.net.Transport;

/**
 * Used on both the client and the server to handle the encoding and decoding of sequenced
 * datagrams.
 */
public class DatagramSequencer
{
    /**
     * Creates a new sequencer that will read and write from the specified streams.
     */
    public DatagramSequencer (UnreliableObjectInputStream uin, UnreliableObjectOutputStream uout)
    {
        _uin = uin;
        _uout = uout;
    }

    /**
     * Writes a datagram to the underlying stream.
     */
    public synchronized void writeDatagram (Message datagram)
        throws IOException
    {
        // first write the sequence and acknowledge numbers
        _uout.writeInt(++_lastNumber);
        _uout.writeInt(_lastReceived);

        // make sure the mapped class set is clear
        Set<Class<?>> mappedClasses = _uout.getMappedClasses();
        mappedClasses.clear();

        // write the object
        _uout.writeObject(datagram);

        // if we wrote any class mappings, we will keep them in the send record
        if (mappedClasses.isEmpty()) {
            mappedClasses = null;
        } else {
            _uout.setMappedClasses(new HashSet<Class<?>>());
        }

        // record the transmission
        _sendrecs.add(new SendRecord(_lastNumber, mappedClasses));
    }

    /**
     * Reads a datagram from the underlying stream.
     *
     * @return the contents of the datagram, or <code>null</code> if the datagram was received
     * out-of-order.
     */
    public synchronized Message readDatagram ()
        throws IOException, ClassNotFoundException
    {
        // read in the sequence number and determine if it's out-of-order
        int number = _uin.readInt();
        if (number <= _lastReceived) {
            return null;
        }
        _lastReceived = number;

        // read the acknowledge number and process all send records up to that one
        int received = _uin.readInt();
        int remove = 0;
        for (int ii = 0, nn = _sendrecs.size(); ii < nn; ii++) {
            SendRecord sendrec = _sendrecs.get(ii);
            if (sendrec.number > received) {
                break;
            }
            remove++;
            if (sendrec.mappedClasses != null) {
                _uout.noteMappingsReceived(sendrec.mappedClasses);
            }
        }
        _sendrecs.subList(0, remove).clear();

        // read the contents of the datagram, note the transport, and return
        Message datagram = (Message)_uin.readObject();
        datagram.setTransport(Transport.UNRELIABLE_ORDERED);
        return datagram;
    }

    /**
     * A record of a sent datagram.
     */
    protected static class SendRecord
    {
        /** The sequence number of the datagram. */
        public int number;

        /** The set of classes for which mappings were included in the datagram (or
         * <code>null</code> for none). */
        public Set<Class<?>> mappedClasses;

        public SendRecord (int number, Set<Class<?>> mappedClasses)
        {
            this.number = number;
            this.mappedClasses = mappedClasses;
        }
    }

    /** The underlying input stream. */
    protected UnreliableObjectInputStream _uin;

    /** The underlying output stream. */
    protected UnreliableObjectOutputStream _uout;

    /** The last sequence number written. */
    protected int _lastNumber;

    /** The most recent sequence number received. */
    protected int _lastReceived;

    /** Records of datagrams sent. */
    protected ArrayList<SendRecord> _sendrecs = new ArrayList<SendRecord>();
}
