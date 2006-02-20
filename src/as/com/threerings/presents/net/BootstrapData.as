package com.threerings.presents.net {

import flash.util.trace;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.util.StreamableArrayList;

/**
 * A BoostrapData object is communicated back to the client
 * after authentication has succeeded and after the server is fully
 * prepared to deal with the client. It contains information the client
 * will need to interact with the server.
 */
public class BootstrapData
    implements Streamable
{
    /** The oid of this client's associated distributed object. */
    public var clientOid :int;

    /** A list of handles to invocation services. */
    public var services :StreamableArrayList;

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        trace("This is client code: BootstrapData shouldn't be written");
        //out.writeShort(messageId);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        clientOid = ins.readInt();
        services = ins.readObject();
    }
}
}
