//
// $Id: PongResponse.java,v 1.7 2002/05/28 21:56:38 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.Log;

public class PongResponse extends DownstreamMessage
{
    /** The code for a pong response. */
    public static final short TYPE = TYPE_BASE + 5;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public PongResponse ()
    {
        super();
    }

    /**
     * Constructs a pong response which will use the supplied ping time to
     * establish the end-to-end processing delay introduced by the server.
     */
    public PongResponse (long pingStamp)
    {
        // save this for when we are serialized in preparation for
        // delivery over the network
        _pingStamp = pingStamp;
    }

    /**
     * Returns the time at which this packet was packed for delivery in
     * the time frame of the server that sent the packet.
     */
    public long getPackStamp ()
    {
        return _packStamp;
    }

    /**
     * Returns the number of milliseconds that elapsed between the time
     * that the ping which instigated this pong was read from the network
     * and the time that this pong was written to the network.
     */
    public int getProcessDelay ()
    {
        return _processDelay;
    }

    /**
     * Returns a timestamp that was obtained when this packet was decoded
     * by the low-level networking code.
     */
    public long getUnpackStamp ()
    {
        return _unpackStamp;
    }

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);

        // make a note of the time at which we were packed
        _packStamp = System.currentTimeMillis();
        out.writeLong(_packStamp);

        // the time spent between unpacking the ping and packing the pong
        // is the processing delay
        if (_pingStamp == 0L) {
            Log.warning("Pong response written that was not constructed " +
                        "with a valid ping stamp [rsp=" + this + "].");
            _processDelay = 0;
        } else {
            _processDelay = (int)(_packStamp - _pingStamp);
        }
        out.writeInt(_processDelay);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);

        // grab a timestamp noting when we were decoded from a raw buffer
        // after being received over the network
        _unpackStamp = System.currentTimeMillis();

        // read in our time stamps
        _packStamp = in.readLong();
        _processDelay = in.readInt();
    }

    public short getType ()
    {
        return TYPE;
    }

    public String toString ()
    {
        return "[type=PONG, msgid=" + messageId + "]";
    }

    /** The ping unpack stamp provided at construct time to this pong
     * response; only valid on the sending process, not the receiving
     * process. */
    protected long _pingStamp;

    /** The timestamp obtained immediately before this packet was sent out
     * over the network. */
    protected long _packStamp;

    /** The delay in milliseconds between the time that the ping request
     * was read from the network and the time the pong response was
     * written to the network. */
    protected int _processDelay;

    /** A time stamp obtained when we unserialize this object (the intent
     * is to get a timestamp as close as possible to when the packet was
     * received on the network). */
    protected long _unpackStamp;
}
