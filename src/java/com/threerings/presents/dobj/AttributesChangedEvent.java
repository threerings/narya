//
// $Id: AttributesChangedEvent.java,v 1.12 2004/08/27 02:20:20 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.dobj;

import com.samskivert.util.StringUtil;

/**
 * An attribute<em>s</em> changed event is dispatched when multiple
 * attributes of a distributed object have been changed in a single
 * transaction.
 *
 * @see DObjectManager#postEvent
 */
public class AttributesChangedEvent extends DEvent
{
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
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public AttributesChangedEvent ()
    {
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

    protected void toString (StringBuffer buf)
    {
        buf.append("CHANGES:");
        super.toString(buf);
        buf.append(", names=");
        StringUtil.toString(buf, _names);
        buf.append(", values=");
        StringUtil.toString(buf, _values);
    }

    protected int _count;
    protected String[] _names;
    protected Object[] _values;
}
