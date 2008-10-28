//
// $Id$

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
        o1.setLocalAttribute(BaseLocalAttr.class, base);
        assertSame(o1.getLocalAttribute(BaseLocalAttr.class), base);
    }

    @Test public void testPoly ()
    {
        // polymorphic usage
        DObject o2 = new DObject();
        DerivedLocalAttr derived = new DerivedLocalAttr();
        o2.setLocalAttribute(DerivedLocalAttr.class, derived);
        BaseLocalAttr upcasted = derived;
        assertSame(o2.getLocalAttribute(DerivedLocalAttr.class), derived);
        assertSame(o2.getLocalAttribute(BaseLocalAttr.class), upcasted);

        // cannot overwrite already set attribute
        DObject o3 = new DObject();
        o3.setLocalAttribute(DerivedLocalAttr.class, derived);
        try {
            o3.setLocalAttribute(DerivedLocalAttr.class, new DerivedLocalAttr());
            fail("Object allowed attribute overwrite.");
        } catch (IllegalStateException ise) {
            // good
        }
        try {
            o3.setLocalAttribute(BaseLocalAttr.class, new BaseLocalAttr());
            fail("Object allowed polymorhic attribute overwrite.");
        } catch (IllegalStateException ise) {
            // good
        }
    }

    @Test public void testSetClearSet ()
    {
        DObject o1 = new DObject();
        BaseLocalAttr base = new BaseLocalAttr();
        o1.setLocalAttribute(BaseLocalAttr.class, base);
        assertSame(o1.getLocalAttribute(BaseLocalAttr.class), base);
        o1.setLocalAttribute(BaseLocalAttr.class, null);
        assertNull(o1.getLocalAttribute(BaseLocalAttr.class));
        BaseLocalAttr nbase = new BaseLocalAttr();
        o1.setLocalAttribute(BaseLocalAttr.class, nbase);
        assertNotSame(o1.getLocalAttribute(BaseLocalAttr.class), base);
        assertSame(o1.getLocalAttribute(BaseLocalAttr.class), nbase);
    }
}
