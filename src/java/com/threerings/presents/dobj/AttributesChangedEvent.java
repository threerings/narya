//
// $Id: AttributesChangedEvent.java,v 1.3 2001/06/11 17:44:04 mdb Exp $

package com.threerings.cocktail.cher.dobj;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An attribute<em>s</em> changed event is dispatched when multiple
 * attributes of a distributed object have been changed in a single
 * transaction.
 *
 * @see DObjectManager.postEvent
 */
public class AttributesChangedEvent extends TypedEvent
{
    /** The typed object code for this event. */
    public static final short TYPE = TYPE_BASE + 2;

    /**
     * Constructs a new attribute changed event on the specified target
     * object with the supplied attribute name and value.
     *
     * @param targetOid the object id of the object whose attribute has
     * changed.
     * @param count the number of attributes that have changed (the length
     * of the <code>names</code> and <code>values</code> arrays need not
     * be exactly equal to the number of attributes changed, there can be
     * extra space at the end).
     * @param names the names of the attributes (data members) that have
     * changed.
     * @param values the new values of the attributes (in the case of
     * primitive types, the reflection-defined object-alternative is
     * used).
     */
    public AttributesChangedEvent (int targetOid, int count,
                                   String[] names, Object[] values)
    {
        super(targetOid);
        _count = count;
        _names = names;
        _values = values;
    }

    /**
     * Returns the number of attributes that have changed.
     */
    public int getCount ()
    {
        return _count;
    }

    /**
     * Returns the name of the index<em>th</em> attribute that has
     * changed. This value is not range checked, so be sure it is between
     * <code>0</code> and <code>getCount()-1</code>. You may not receive
     * an <code>ArrayIndexOutOfBounds</code> exception if you exceed
     * <code>getCount()</code> but rather get a null name.
     */
    public String getName (int index)
    {
        return _names[index];
    }

    /**
     * Returns the new value of the index<em>th</em> attribute. This value
     * is not range checked, so be sure it is between <code>0</code> and
     * <code>getCount()-1</code>. You may not receive an
     * <code>ArrayIndexOutOfBounds</code> exception if you exceed
     * <code>getCount()</code> but rather get a null value.
     */
    public Object getValue (int index)
    {
        return _values[index];
    }

    /**
     * Returns the new value of the index<em>th</em> attribute as a
     * short. This will fail if the attribute in question is not a short.
     * The <code>index</code> parameter is not range checked, so be sure
     * it is between <code>0</code> and <code>getCount()-1</code>. You may
     * not receive an <code>ArrayIndexOutOfBounds</code> exception if you
     * exceed <code>getCount()</code> but rather get a
     * <code>NullPointerException</code>.
     */
    public short getShortValue (int index)
    {
        return ((Short)_values[index]).shortValue();
    }

    /**
     * Returns the new value of the index<em>th</em> attribute as an
     * int. This will fail if the attribute in question is not an int.
     * The <code>index</code> parameter is not range checked, so be sure
     * it is between <code>0</code> and <code>getCount()-1</code>. You may
     * not receive an <code>ArrayIndexOutOfBounds</code> exception if you
     * exceed <code>getCount()</code> but rather get a
     * <code>NullPointerException</code>.
     */
    public int getIntValue (int index)
    {
        return ((Integer)_values[index]).intValue();
    }

    /**
     * Returns the new value of the index<em>th</em> attribute as a
     * long. This will fail if the attribute in question is not a long.
     * The <code>index</code> parameter is not range checked, so be sure
     * it is between <code>0</code> and <code>getCount()-1</code>. You may
     * not receive an <code>ArrayIndexOutOfBounds</code> exception if you
     * exceed <code>getCount()</code> but rather get a
     * <code>NullPointerException</code>.
     */
    public long getLongValue (int index)
    {
        return ((Long)_values[index]).longValue();
    }

    /**
     * Returns the new value of the index<em>th</em> attribute as a
     * float. This will fail if the attribute in question is not a float.
     * The <code>index</code> parameter is not range checked, so be sure
     * it is between <code>0</code> and <code>getCount()-1</code>. You may
     * not receive an <code>ArrayIndexOutOfBounds</code> exception if you
     * exceed <code>getCount()</code> but rather get a
     * <code>NullPointerException</code>.
     */
    public float getFloatValue (int index)
    {
        return ((Float)_values[index]).floatValue();
    }

    /**
     * Returns the new value of the index<em>th</em> attribute as a
     * double. This will fail if the attribute in question is not a
     * double. The <code>index</code> parameter is not range checked, so
     * be sure it is between <code>0</code> and <code>getCount()-1</code>.
     * You may not receive an <code>ArrayIndexOutOfBounds</code> exception
     * if you exceed <code>getCount()</code> but rather get a
     * <code>NullPointerException</code>.
     */
    public double getDoubleValue (int index)
    {
        return ((Double)_values[index]).doubleValue();
    }

    /**
     * Applies this attribute change to the object.
     */
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // pass the new values on to the object
        for (int i = 0; i < _count; i++) {
            target.setAttribute(_names[i], _values[i]);
        }
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
        out.writeInt(_count);
        for (int i = 0; i < _count; i++) {
            out.writeUTF(_names[i]);
            // out.write...(_values[i]);
        }
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _count = in.readInt();
        _names = new String[_count];
        _values = new Object[_count];
        for (int i = 0; i < _count; i++) {
            _names[i] = in.readUTF();
            // _values[i] = ...
        }
    }

    protected int _count;
    protected String[] _names;
    protected Object[] _values;
}
