//
// $Id: ElementUpdatedEvent.java,v 1.2 2002/07/23 05:52:48 mdb Exp $

package com.threerings.presents.dobj;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.Log;

/**
 * An element updated event is dispatched when an element of an array
 * field in a distributed object is updated. It can also be constructed to
 * request the update of an entry and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class ElementUpdatedEvent extends DEvent
{
    /**
     * Constructs a new element updated event on the specified target
     * object with the supplied attribute name, element and index.
     *
     * @param targetOid the object id of the object whose attribute has
     * changed.
     * @param name the name of the attribute (data member) for which an
     * element has changed.
     * @param value the new value of the element (in the case of primitive
     * types, the reflection-defined object-alternative is used).
     * @param index the index in the array of the updated element.
     */
    public ElementUpdatedEvent (
        int targetOid, String name, Object value, int index)
    {
        super(targetOid);
        _name = name;
        _value = value;
        _index = index;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public ElementUpdatedEvent ()
    {
    }

    /**
     * Returns the name of the attribute that has changed.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Returns the new value of the element.
     */
    public Object getValue ()
    {
        return _value;
    }

    /**
     * Returns the index of the element.
     */
    public int getIndex ()
    {
        return _index;
    }

    /**
     * Returns the new value of the element as a short. This will fail if
     * the element in question is not a short.
     */
    public short getShortValue ()
    {
        return ((Short)_value).shortValue();
    }

    /**
     * Returns the new value of the element as an int. This will fail if
     * the element in question is not an int.
     */
    public int getIntValue ()
    {
        return ((Integer)_value).intValue();
    }

    /**
     * Returns the new value of the element as a long. This will fail if
     * the element in question is not a long.
     */
    public long getLongValue ()
    {
        return ((Long)_value).longValue();
    }

    /**
     * Returns the new value of the element as a float. This will fail if
     * the element in question is not a float.
     */
    public float getFloatValue ()
    {
        return ((Float)_value).floatValue();
    }

    /**
     * Returns the new value of the element as a double. This will fail if
     * the element in question is not a double.
     */
    public double getDoubleValue ()
    {
        return ((Double)_value).doubleValue();
    }

    /**
     * Applies this element update to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        try {
            // fetch the array field from the object
            Field field = target.getClass().getField(_name);
            Class ftype = field.getType();

            // sanity check
            if (!ftype.isArray()) {
                String msg = "Requested to set element on non-array field.";
                throw new Exception(msg);
            }

            // we don't do any magical expansion or any funny business;
            // the array should be big enough to contain the value being
            // updated or we'll throw an ArrayIndexOutOfBoundsException
            Array.set(field.get(target), _index, _value);
            return true;

        } catch (Exception e) {
            String msg = "Error updating element [field=" + _name +
                ", index=" + _index + "]";
            throw new ObjectAccessException(msg, e);
        }
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeUTF(_name);
        out.writeObject(_value);
        out.writeInt(_index);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _name = in.readUTF();
        _value = in.readObject();
        _index = in.readInt();
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof ElementUpdateListener) {
            ((ElementUpdateListener)listener).elementUpdated(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("UPDATE:");
        super.toString(buf);
        buf.append(", name=").append(_name);
        buf.append(", value=");
        StringUtil.toString(buf, _value);
        buf.append(", index=").append(_index);
    }

    protected String _name;
    protected Object _value;
    protected int _index;
}
