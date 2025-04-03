//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests distributed object local attributes.
 */
public class LocalAttrTest
{
    public static class BaseLocalAttr
    {
    }

    public static class DerivedLocalAttr extends BaseLocalAttr
    {
    }

    @Test public void testSetGet ()
    {
        DObject o1 = new DObject();
        BaseLocalAttr base = new BaseLocalAttr();
        o1.setLocal(BaseLocalAttr.class, base);
        assertSame(o1.getLocal(BaseLocalAttr.class), base);
    }

    @Test public void testPoly ()
    {
        // polymorphic usage
        DObject o2 = new DObject();
        DerivedLocalAttr derived = new DerivedLocalAttr();
        o2.setLocal(DerivedLocalAttr.class, derived);
        BaseLocalAttr upcasted = derived;
        assertSame(o2.getLocal(DerivedLocalAttr.class), derived);
        assertSame(o2.getLocal(BaseLocalAttr.class), upcasted);

        // cannot overwrite already set attribute
        DObject o3 = new DObject();
        o3.setLocal(DerivedLocalAttr.class, derived);
        try {
            o3.setLocal(DerivedLocalAttr.class, new DerivedLocalAttr());
            fail("Object allowed attribute overwrite.");
        } catch (IllegalStateException ise) {
            // good
        }
        try {
            o3.setLocal(BaseLocalAttr.class, new BaseLocalAttr());
            fail("Object allowed polymorhic attribute overwrite.");
        } catch (IllegalStateException ise) {
            // good
        }
    }

    @Test public void testSetClearSet ()
    {
        DObject o1 = new DObject();
        BaseLocalAttr base = new BaseLocalAttr();
        o1.setLocal(BaseLocalAttr.class, base);
        assertSame(o1.getLocal(BaseLocalAttr.class), base);
        o1.setLocal(BaseLocalAttr.class, null);
        assertNull(o1.getLocal(BaseLocalAttr.class));
        BaseLocalAttr nbase = new BaseLocalAttr();
        o1.setLocal(BaseLocalAttr.class, nbase);
        assertNotSame(o1.getLocal(BaseLocalAttr.class), base);
        assertSame(o1.getLocal(BaseLocalAttr.class), nbase);
    }
}
