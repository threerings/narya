package com.threerings.presents.net {

import flash.util.trace;

import com.threerings.io.Streamable;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class UpstreamMessage
    implements Streamable
{
    /** This is a unique (within the context of a reasonable period
    * of time) identifier assigned to each upstream message. The message ids
    * are used to correlate a downstream response message to the 
    * appropriate upstream request message. */
    public var messageId :int;

    /**
     * Construct an upstream message.
     */
    public function UpstreamMessage ()
    {
        // automatically generate a valid message id
        this.messageId = nextMessageId();
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeShort(messageId);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        trace("This is client code: Upstream messages shouldn't be read");
        //messageId = ins.readShort();
    }

    /**
     * Returns the next message id suitable for use by an upstream message./
     */
    protected static function nextMessageId () :int
    {
        _nextMessageId = (_nextMessageId + 1) % JavaConstants.SHORT_MAX_VALUE;
        return _nextMessageId;
    }

    /** This is used to generate monotonically increasing message ids on the
     * client as new messages are generated. */
    protected static var _nextMessageId :int;
}
}
