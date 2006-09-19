//
// $Id$
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

package com.threerings.presents.data;

import java.util.ArrayList;

import com.threerings.presents.dobj.*;

/**
 * A test distributed object.
 */
public class TestObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>foo</code> field. */
    public static final String FOO = "foo";

    /** The field name of the <code>bar</code> field. */
    public static final String BAR = "bar";

    /** The field name of the <code>ints</code> field. */
    public static final String INTS = "ints";

    /** The field name of the <code>strings</code> field. */
    public static final String STRINGS = "strings";

    /** The field name of the <code>list</code> field. */
    public static final String LIST = "list";

    /** The field name of the <code>longs</code> field. */
    public static final String LONGS = "longs";
    // AUTO-GENERATED: FIELDS END

    public int foo;

    public String bar;

    public int[] ints = new int[5];

    public String[] strings = new String[5];

    public OidList list = new OidList();

    public ArrayList<Long> longs = new ArrayList<Long>();

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>foo</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setFoo (int value)
    {
        int ovalue = this.foo;
        requestAttributeChange(
            FOO, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.foo = value;
    }

    /**
     * Requests that the <code>bar</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setBar (String value)
    {
        String ovalue = this.bar;
        requestAttributeChange(
            BAR, value, ovalue);
        this.bar = value;
    }

    /**
     * Requests that the <code>ints</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setInts (int[] value)
    {
        int[] ovalue = this.ints;
        requestAttributeChange(
            INTS, value, ovalue);
        this.ints = (value == null) ? null : (int[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>ints</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setIntsAt (int value, int index)
    {
        int ovalue = this.ints[index];
        requestElementUpdate(
            INTS, index, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.ints[index] = value;
    }

    /**
     * Requests that the <code>strings</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setStrings (String[] value)
    {
        String[] ovalue = this.strings;
        requestAttributeChange(
            STRINGS, value, ovalue);
        this.strings = (value == null) ? null : (String[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>strings</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setStringsAt (String value, int index)
    {
        String ovalue = this.strings[index];
        requestElementUpdate(
            STRINGS, index, value, ovalue);
        this.strings[index] = value;
    }

    /**
     * Requests that <code>oid</code> be added to the <code>list</code>
     * oid list. The list will not change until the event is actually
     * propagated through the system.
     */
    public void addToList (int oid)
    {
        requestOidAdd(LIST, oid);
    }

    /**
     * Requests that <code>oid</code> be removed from the
     * <code>list</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromList (int oid)
    {
        requestOidRemove(LIST, oid);
    }

    /**
     * Requests that the <code>longs</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setLongs (ArrayList<java.lang.Long> value)
    {
        ArrayList<java.lang.Long> ovalue = this.longs;
        requestAttributeChange(
            LONGS, value, ovalue);
        this.longs = value;
    }
    // AUTO-GENERATED: METHODS END
}
