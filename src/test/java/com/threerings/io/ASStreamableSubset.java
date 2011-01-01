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

package com.threerings.io;

//
//$Id$
//
//Narya library - tools for developing networked games
//Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
//http://www.threerings.net/code/narya/
//
//This library is free software; you can redistribute it and/or modify it
//under the terms of the GNU Lesser General Public License as published
//by the Free Software Foundation; either version 2.1 of the License, or
//(at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.samskivert.util.Mapping;

public class ASStreamableSubset extends SimpleStreamableObject
{
    public boolean bool1 = true;
    public short short2 = 2;
    public int int3 = 3;
    public long long4 = 4;
    public float float5 = 5;
    public double double6 = 6;
    public char char7 = (char)7;
    public byte byte8 = 8;

    public String string1 = "one";
    public String nullString1;

    public boolean[] bools = new boolean[] { true, false, true };
    public byte[] bytes = new byte[] { 1, 2, 3 };
    public int[] ints = new int[] { 1, 2, 3 };

    public boolean[] nullBools;
    public byte[] nullBytes;
    public int[] nullInts;

    public List<String> strings = Lists.newArrayList("one", "two", "three");
    public List<String> nullStrings;

    public Map<String, String> stringMap = Mapping.of("one", "1", "two", "2", "three", "3");
    public Map<String, Integer> stringIntMap = Mapping.of("one", 1, "two", 2, "three", 3);
    public Map<String, String> nullStringMap;

    @Override
    public boolean equals (Object other)
    {
        if (!(other instanceof ASStreamableSubset)) {
            return false;
        }
        ASStreamableSubset ow = (ASStreamableSubset)other;
        return bool1 == ow.bool1 && byte8 == ow.byte8 && char7 == ow.char7 && short2 == ow.short2
            && int3 == ow.int3 && long4 == ow.long4
            && float5 == ow.float5
            && double6 == ow.double6
            &&

            Objects.equal(string1, ow.string1)
            && Objects.equal(nullString1, ow.nullString1)
            &&

            Arrays.equals(bools, ow.bools) && Arrays.equals(bytes, ow.bytes)
            && Arrays.equals(ints, ow.ints) &&

            Arrays.equals(nullBools, ow.nullBools) && Arrays.equals(nullBytes, ow.nullBytes)
            && Arrays.equals(nullInts, ow.nullInts)
            &&

            Objects.equal(strings, ow.strings) && Objects.equal(nullStrings, ow.nullStrings)
            &&

            Objects.equal(stringMap, ow.stringMap) && Objects.equal(stringIntMap, ow.stringIntMap)
            && Objects.equal(nullStringMap, ow.nullStringMap);
    }
}
