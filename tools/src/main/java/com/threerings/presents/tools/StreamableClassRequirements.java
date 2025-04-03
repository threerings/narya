//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.List;

import com.google.common.collect.Lists;

import com.threerings.io.Streamable;

public class StreamableClassRequirements
{
    public final List<Field> streamedFields = Lists.newArrayList();

    public final boolean superclassStreamable;

    public final Class<?> klass;

    public StreamableClassRequirements(Class<?> klass)
    {
        this.klass = klass;
        superclassStreamable =
            klass.getSuperclass() != null && Streamable.class.isAssignableFrom(klass.getSuperclass());
        for (Field field : klass.getDeclaredFields()) {
            int mods = field.getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
                continue;
            }
            streamedFields.add(field);
        }
    }
}
