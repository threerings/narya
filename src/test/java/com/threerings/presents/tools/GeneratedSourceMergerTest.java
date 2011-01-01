//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GeneratedSourceMergerTest
{
    @Test
    public void mergeNothing ()
        throws Exception
    {
        new GeneratedSourceMerger().merge("", "");
    }

    @Test
    public void mergeUpdatedSection ()
        throws Exception
    {
        String modified = before + "// GENERATED VARIABLE START\n" +
        "var s :String = 'byte';\n" +
        "// GENERATED VARIABLE END\n" + after;

        assertEquals(generated, new GeneratedSourceMerger().merge(generated, modified));
    }

    @Test
    public void mergeMissingSection ()
        throws Exception
    {
        assertEquals(section + before, new GeneratedSourceMerger().merge(generated, before));
    }

    @Test
    public void ignoreDisabledSection ()
        throws Exception
    {
        String disabled = "// GENERATED VARIABLE DISABLED\n" + before;
        assertEquals(disabled, new GeneratedSourceMerger().merge(generated, disabled));
    }

    @Test
    public void dropOldSection ()
        throws Exception
    {
        String previous = "// GENERATED PREVIOUS START\n" +
            "var noLongerNeeded :String = 'hi';\n" +
            "// GENERATED PREVIOUS END\n" +
            generated;
        assertEquals(generated, new GeneratedSourceMerger().merge(generated, previous));
    }

    String section = "// GENERATED VARIABLE START\n" +
     "var s :String = 'hi';\n" +
    "// GENERATED VARIABLE END\n";

    String before = "var r :int = 7;\n";
    String after = "var t :Array = [];\n";
    String generated = before + section + after;

}
