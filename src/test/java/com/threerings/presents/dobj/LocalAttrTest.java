//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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
