//
// $Id: AttributeChangedEvent.java,v 1.1 2001/06/01 07:12:13 mdb Exp $

package com.threerings.cocktail.cher.dobj;

/**
 * An attribute changed event is dispatched when a single attribute of a
 * distributed object has changed. It can also be constructed to request
 * an attribute change on an object and posted to the dobjmgr.
 *
 * @see DObjectManager.postEvent
 */
public class AttributeChangedEvent extends DEvent
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
        super(targetOid);
        _name = name;
        _value = value;
    }

    /**
     * Returns the name of the attribute that has changed.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Returns the new value of the attribute.
     */
    public Object getValue ()
    {
        return _value;
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

    protected String _name;
    protected Object _value;
}
