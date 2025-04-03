//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.data;

import java.util.ArrayList;
import javax.annotation.Generated;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.OidList;

/**
 * A test distributed object.
 */
public class TestObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>foo</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String FOO = "foo";

    /** The field name of the <code>bar</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String BAR = "bar";

    /** The field name of the <code>ints</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String INTS = "ints";

    /** The field name of the <code>strings</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String STRINGS = "strings";

    /** The field name of the <code>list</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String LIST = "list";

    /** The field name of the <code>longs</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setInts (int[] value)
    {
        int[] ovalue = this.ints;
        requestAttributeChange(
            INTS, value, ovalue);
        this.ints = (value == null) ? null : value.clone();
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setStrings (String[] value)
    {
        String[] ovalue = this.strings;
        requestAttributeChange(
            STRINGS, value, ovalue);
        this.strings = (value == null) ? null : value.clone();
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
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
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToList (int oid)
    {
        requestOidAdd(LIST, list, oid);
    }

    /**
     * Requests that <code>oid</code> be removed from the
     * <code>list</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromList (int oid)
    {
        requestOidRemove(LIST, list, oid);
    }

    /**
     * Requests that the <code>longs</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setLongs (ArrayList<Long> value)
    {
        ArrayList<Long> ovalue = this.longs;
        requestAttributeChange(
            LONGS, value, ovalue);
        this.longs = value;
    }
    // AUTO-GENERATED: METHODS END
}
