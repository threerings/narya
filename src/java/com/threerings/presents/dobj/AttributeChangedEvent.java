//
// $Id: AttributeChangedEvent.java,v 1.14 2002/12/20 23:29:04 mdb Exp $

package com.threerings.presents.dobj;

import com.samskivert.util.StringUtil;

/**
 * An attribute changed event is dispatched when a single attribute of a
 * distributed object has changed. It can also be constructed to request
 * an attribute change on an object and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class AttributeChangedEvent extends NamedEvent
{
    /**
     * Constructs a new attribute changed event on the specified target
     * object with the supplied attribute name and value.
     *
     * @param targetOid the object id of the object whose attribute has
     * changed.
     * @param name the name of the attribute (data member) that has
     * changed.
     * @param value the new value of the attribute (in the case of
     * primitive types, the reflection-defined object-alternative is
     * used).
     */
    public AttributeChangedEvent (int targetOid, String name, Object value)
    {
        super(targetOid, name);
        _value = value;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public AttributeChangedEvent ()
    {
    }

    /**
     * Returns the new value of the attribute.
     */
    public Object getValue ()
    {
        return _value;
    }

    /**
     * Returns the value of the attribute prior to the application of this
     * event.
     */
    public Object getOldValue ()
    {
        return _oldValue;
    }

    /**
     * Returns the new value of the attribute as a short. This will fail
     * if the attribute in question is not a short.
     */
    public short getShortValue ()
    {
        return ((Short)_value).shortValue();
    }

    /**
     * Returns the new value of the attribute as an int. This will fail if
     * the attribute in question is not an int.
     */
    public int getIntValue ()
    {
        return ((Integer)_value).intValue();
    }

    /**
     * Returns the new value of the attribute as a long. This will fail if
     * the attribute in question is not a long.
     */
    public long getLongValue ()
    {
        return ((Long)_value).longValue();
    }

    /**
     * Returns the new value of the attribute as a float. This will fail
     * if the attribute in question is not a float.
     */
    public float getFloatValue ()
    {
        return ((Float)_value).floatValue();
    }

    /**
     * Returns the new value of the attribute as a double. This will fail
     * if the attribute in question is not a double.
     */
    public double getDoubleValue ()
    {
        return ((Double)_value).doubleValue();
    }

    /**
     * Applies this attribute change to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // grab the previous value
        _oldValue = target.getAttribute(_name);
        // pass the new value on to the object
        target.setAttribute(_name, _value);
        return true;
    }

    // documentation inherited
    protected void notifyListener (Object listener)
    {
        if (listener instanceof AttributeChangeListener) {
            ((AttributeChangeListener)listener).attributeChanged(this);
        }
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append("CHANGE:");
        super.toString(buf);
        buf.append(", value=");
        StringUtil.toString(buf, _value);
    }

    protected Object _value;
    protected transient Object _oldValue;
}
