//
// $Id: TestObject.java,v 1.3 2002/03/19 01:10:03 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.dobj.*;

/**
 * A test distributed object.
 */
public class TestObject extends DObject
{
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

    public int foo;
    public String bar;
    public int[] ints = new int[5];
    public String[] strings = new String[5];
    public OidList list = new OidList();

    /**
     * Requests that the <code>foo</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setFoo (int foo)
    {
        this.foo = foo;
        requestAttributeChange(FOO, new Integer(foo));
    }

    /**
     * Requests that the <code>bar</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setBar (String bar)
    {
        this.bar = bar;
        requestAttributeChange(BAR, bar);
    }

    /**
     * Requests that the <code>ints</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setInts (int[] ints)
    {
        this.ints = ints;
        requestAttributeChange(INTS, ints);
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>ints</code> field be set to the specified value. The local
     * value will be updated immediately and an event will be propagated
     * through the system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setIntsAt (int value, int index)
    {
        this.ints[index] = value;
        requestElementUpdate(INTS, new Integer(value), index);
    }

    /**
     * Requests that the <code>strings</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setStrings (String[] strings)
    {
        this.strings = strings;
        requestAttributeChange(STRINGS, strings);
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>strings</code> field be set to the specified value. The local
     * value will be updated immediately and an event will be propagated
     * through the system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setStringsAt (String value, int index)
    {
        this.strings[index] = value;
        requestElementUpdate(STRINGS, value, index);
    }

    /**
     * Requests that the specified oid be added to the
     * <code>list</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    public void addToList (int oid)
    {
        requestOidAdd(LIST, oid);
    }

    /**
     * Requests that the specified oid be removed from the
     * <code>list</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromList (int oid)
    {
        requestOidRemove(LIST, oid);
    }
}
