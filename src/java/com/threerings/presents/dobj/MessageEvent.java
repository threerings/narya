//
// $Id: MessageEvent.java,v 1.9 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;
import java.lang.reflect.Method;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * A message event is used to dispatch a message to all subscribers of a
 * distributed object without actually changing any of the fields of the
 * object. A message has a name, by which different subscribers of the
 * same object can distinguish their different messages, and an array of
 * arguments by which any contents of the message can be delivered.
 *
 * @see DObjectManager#postEvent
 */
public class MessageEvent extends DEvent
{
    /**
     * Constructs a new message event on the specified target object with
     * the supplied name and arguments.
     *
     * @param targetOid the object id of the object whose attribute has
     * changed.
     * @param name the name of the message event.
     * @param args the arguments for this message. This array should
     * contain only values of valid distributed object types.
     */
    public MessageEvent (int targetOid, String name, Object[] args)
    {
        super(targetOid);
        _name = name;
        _args = args;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public MessageEvent ()
    {
    }

    /**
     * Returns the name of the message.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Returns the arguments to this message.
     */
    public Object[] getArgs ()
    {
        return _args;
    }

    /**
     * Replaces the arguments associated with this message event.
     * <em>Note:</em> this should only be called on events that have not
     * yet been dispatched into the distributed object system.
     */
    public void setArgs (Object[] args)
    {
        _args = args;
    }

    /**
     * Applies this attribute change to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // nothing to do here
        return true;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeUTF(_name);
        out.writeObject(_args);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _name = in.readUTF();
        _args = (Object[])in.readObject();
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof MessageListener) {
            ((MessageListener)listener).messageReceived(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("MSG:");
        super.toString(buf);
        buf.append(", name=").append(_name);
        buf.append(", args=").append(StringUtil.toString(_args));
    }

    protected String _name;
    protected Object[] _args;
}
