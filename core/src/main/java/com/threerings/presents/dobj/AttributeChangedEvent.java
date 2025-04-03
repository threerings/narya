//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import java.lang.reflect.Array;

import com.samskivert.util.StringUtil;

/**
 * An attribute changed event is dispatched when a single attribute of a distributed object has
 * changed. It can also be constructed to request an attribute change on an object and posted to
 * the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class AttributeChangedEvent extends NamedEvent
{
    /**
     * Constructs a new attribute changed event on the specified target object with the supplied
     * attribute name and value. <em>Do not construct these objects by hand.</em> Use {@link
     * DObject#changeAttribute} instead.
     *
     * @param targetOid the object id of the object whose attribute has changed.
     * @param name the name of the attribute (data member) that has changed.
     * @param value the new value of the attribute (in the case of primitive types, the
     * reflection-defined object-alternative is used).
     */
    public AttributeChangedEvent (int targetOid, String name, Object value)
    {
        super(targetOid, name);
        _value = value;
    }

    /**
     * Returns the new value of the attribute.
     */
    public Object getValue ()
    {
        return _value;
    }

    /**
     * Returns the value of the attribute prior to the application of this event.
     */
    public Object getOldValue ()
    {
        return _oldValue;
    }

    /**
     * Returns the new value of the attribute as a byte. This will fail if the attribute in
     * question is not a byte.
     */
    public byte getByteValue ()
    {
        return ((Byte)_value).byteValue();
    }

    /**
     * Returns the new value of the attribute as a short. This will fail if the attribute in
     * question is not a short.
     */
    public short getShortValue ()
    {
        return ((Short)_value).shortValue();
    }

    /**
     * Returns the new value of the attribute as an int. This will fail if the attribute in
     * question is not an int.
     */
    public int getIntValue ()
    {
        return ((Integer)_value).intValue();
    }

    /**
     * Returns the new value of the attribute as a long. This will fail if the attribute in
     * question is not a long.
     */
    public long getLongValue ()
    {
        return ((Long)_value).longValue();
    }

    /**
     * Returns the new value of the attribute as a float. This will fail if the attribute in
     * question is not a float.
     */
    public float getFloatValue ()
    {
        return ((Float)_value).floatValue();
    }

    /**
     * Returns the new value of the attribute as a double. This will fail if the attribute in
     * question is not a double.
     */
    public double getDoubleValue ()
    {
        return ((Double)_value).doubleValue();
    }

    @Override
    public boolean alreadyApplied ()
    {
        // if we have an old value, that means we're running on the master server and we have
        // already applied this attribute change to the object
        return (_oldValue != UNSET_OLD_VALUE);
    }

    @Override
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // if we're not already applied, grab the previous value and apply the attribute change
        if (!alreadyApplied()) {
            _oldValue = target.getAttribute(_name);
            Object value = _value;
            if (value != null) {
                Class<?> vclass = value.getClass();
                if (vclass.isPrimitive()) {
                    // do nothing; we check this to avoid the more expensive isAssignableFrom check
                    // on primitives which are far and away the most common case
                } else if (vclass.isArray()) {
                    int length = Array.getLength(value);
                    Object clone = Array.newInstance(vclass.getComponentType(), length);
                    System.arraycopy(value, 0, clone, 0, length);
                    value = clone;
                } else if (DSet.class.isAssignableFrom(vclass)) {
                    value = ((DSet<?>)value).clone();
                }
            }
            // pass the new value on to the object
            target.setAttribute(_name, value);
        }
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        if (listener instanceof AttributeChangeListener) {
            ((AttributeChangeListener)listener).attributeChanged(this);
        }
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        buf.append("CHANGE:");
        super.toString(buf);
        buf.append(", value=");
        StringUtil.toString(buf, _value);
    }

    protected AttributeChangedEvent setOldValue (Object oldValue)
    {
        _oldValue = oldValue;
        return this;
    }

    protected Object _value;
    protected transient Object _oldValue = UNSET_OLD_VALUE;
}
