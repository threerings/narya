//
// $Id: MessageEvent.java,v 1.5 2001/08/11 00:05:58 mdb Exp $

package com.threerings.cocktail.cher.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import com.samskivert.util.StringUtil;
import com.threerings.cocktail.cher.dobj.io.ValueMarshaller;

/**
 * A message event is used to dispatch a message to all subscribers of a
 * distributed object without actually changing any of the fields of the
 * object. A message has a name, by which different subscribers of the
 * same object can distinguish their different messages, and an array of
 * arguments by which any contents of the message can be delivered.
 *
 * @see DObjectManager#postEvent
 */
public class MessageEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 3;

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

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeUTF(_name);
        if (_args != null) {
            out.writeInt(_args.length);
            for (int i = 0; i < _args.length; i++) {
                ValueMarshaller.writeTo(out, _args[i]);
            }
        } else {
            out.writeInt(0);
        }
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _name = in.readUTF();
        int args = in.readInt();
        if (args > 0) {
            _args = new Object[args];
            for (int i = 0; i < args; i++) {
                _args[i] = ValueMarshaller.readFrom(in);
            }
        }
    }

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
