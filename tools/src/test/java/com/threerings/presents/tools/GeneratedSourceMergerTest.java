//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
