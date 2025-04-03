//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.tools.cpp;

import java.lang.reflect.Field;
import java.io.IOException;

public class CPPField
{
    public final String name;
    public final CPPType type;
    public final String reader;
    public final String writer;

    public CPPField (Field f)
        throws IOException
    {
        type = new CPPType(f.getGenericType());
        name = f.getName().startsWith("_") ? f.getName().substring(1) : f.getName();
        if (type.fixed != null) {
            reader = type.cast + "(in.read" + type.interpreter + "(" + type.fixed + "))";
        } else if (type.interpreter.equals("Field")) {
            reader = "in.readField< " + type.getWithoutShared() + " >()";
        } else {
            reader = type.cast + "(in.read" + type.interpreter + "())";
        }
        writer = "out.write" + type.interpreter + "(" + name
            + (type.fixed == null ? "" : ", " + type.fixed) + ")";
    }
}
