//
// $Id: TestObject.java,v 1.1 2002/02/08 23:17:38 mdb Exp $

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

    /** The field name of the <code>list</code> field. */
    public static final String LIST = "list";

    public int foo;
    public String bar;
    public OidList list = new OidList();

    /**
     * Requests that the <code>foo</code> field be set to the specified
     * value.
     */
    public void setFoo (int foo)
    {
        requestAttributeChange(FOO, new Integer(foo));
    }

    /**
     * Requests that the <code>foo</code> field be set to the
     * specified value and immediately updates the state of the object
     * to reflect the change. This should <em>only</em> be called on the
     * server and only then if you know what you're doing.
     */
    public void setFooImmediate (int foo)
    {
        this.foo = foo;
        requestAttributeChange(FOO, new Integer(foo));
    }

    /**
     * Requests that the <code>bar</code> field be set to the specified
     * value.
     */
    public void setBar (String bar)
    {
        requestAttributeChange(BAR, bar);
    }

    /**
     * Requests that the <code>bar</code> field be set to the
     * specified value and immediately updates the state of the object
     * to reflect the change. This should <em>only</em> be called on the
     * server and only then if you know what you're doing.
     */
    public void setBarImmediate (String bar)
    {
        this.bar = bar;
        requestAttributeChange(BAR, bar);
    }

    /**
     * Requests that the specified oid be added to the
     * <code>list</code> oid list.
     */
    public void addToList (int oid)
    {
        requestOidAdd(LIST, oid);
    }

    /**
     * Requests that the specified oid be removed from the
     * <code>list</code> oid list.
     */
    public void removeFromList (int oid)
    {
        requestOidRemove(LIST, oid);
    }
}
