//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import com.threerings.util.ActionScript;
import com.threerings.util.StreamableArrayList;
import com.threerings.util.StreamableHashMap;

public class ActionScriptUtils
{
    protected static boolean needsActionScriptImport (Class<?> type, boolean isField)
    {
        if (type.isArray()) {
            return Byte.TYPE.equals(type.getComponentType()) || isField;
        }
        return (Long.TYPE.equals(type) || !type.isPrimitive()) && !String.class.equals(type);
    }

    protected static String addImportAndGetShortType (Class<?> type, boolean isField,
        Set<String> imports)
    {
        String full = toActionScriptType(type, isField);
        if (needsActionScriptImport(type, isField)) {
            imports.add(full);
        }
        return Iterables.getLast(DOT_SPLITTER.split(full));
    }

    public static String toReadObject (Class<?> type)
    {
        if (type.equals(String.class)) {
            return "readField(String)";

        } else if (type.equals(Integer.class) ||
                   type.equals(Short.class) ||
                   type.equals(Byte.class)) {
            String name = ActionScriptSource.toSimpleName(type.getName());
            return "readField(" + name + ").value";

        } else if (type.equals(Long.class)) {
            String name = ActionScriptSource.toSimpleName(type.getName());
            return "readField(" + name + ")";

        } else if (type.equals(Boolean.TYPE)) {
            return "readBoolean()";

        } else if (type.equals(Byte.TYPE)) {
            return "readByte()";

        } else if (type.equals(Short.TYPE) || type.equals(Character.TYPE)) {
            return "readShort()";

        } else if (type.equals(Integer.TYPE)) {
            return "readInt()";

        } else if (type.equals(Long.TYPE)) {
            return "readLong()";

        } else if (type.equals(Float.TYPE)) {
            return "readFloat()";

        } else if (type.equals(Double.TYPE)) {
            return "readDouble()";

        } else if (isNaiveMap(type)) {
            return "readField(MapStreamer.INSTANCE)";

        } else if (isNaiveList(type)) {
            return "readField(ArrayStreamer.INSTANCE)";

        } else if (type.isArray()) {
            if (!type.getComponentType().isPrimitive()) {
                return "readObject(TypedArray)";
            } else {
                if (Double.TYPE.equals(type.getComponentType())) {
                    return "readField(TypedArray.getJavaType(Number))";
                } else if (Boolean.TYPE.equals(type.getComponentType())) {
                    return "readField(TypedArray.getJavaType(Boolean))";
                } else if (Integer.TYPE.equals(type.getComponentType())) {
                    return "readField(TypedArray.getJavaType(int))";
                } else if (Byte.TYPE.equals(type.getComponentType())) {
                    return "readField(ByteArray)";
                } else {
                    throw new IllegalArgumentException(type
                        + " isn't supported to stream to actionscript");
                }
            }
        } else {
            return "readObject(" + Iterables.getLast(DOT_SPLITTER.split(toActionScriptType(type, false))) + ")";
        }
    }

    public static String toWriteObject (Class<?> type, String name)
    {
        if (type.equals(Integer.class)) {
            return "writeObject(new Integer(" + name + "))";

        } else if (type.equals(Boolean.TYPE)) {
            return "writeBoolean(" + name + ")";

        } else if (type.equals(Byte.TYPE)) {
            return "writeByte(" + name + ")";

        } else if (type.equals(Short.TYPE) ||
                   type.equals(Character.TYPE)) {
            return "writeShort(" + name + ")";

        } else if (type.equals(Integer.TYPE)) {
            return "writeInt(" + name + ")";

        } else if (type.equals(Long.TYPE)) {
            return "writeLong(" + name + ")";

        } else if (type.equals(Float.TYPE)) {
            return "writeFloat(" + name + ")";

        } else if (type.equals(Double.TYPE)) {
            return "writeDouble(" + name + ")";

        } else if (type.equals(Long.class) ||
                   type.equals(String.class) ||
                   (type.isArray() && type.getComponentType().isPrimitive())) {
            return "writeField(" + name + ")";

        } else if (isNaiveList(type)) {
            return "writeField(" + name + ", ArrayStreamer.INSTANCE)";

        } else if (isNaiveMap(type)) {
            return "writeField(" + name + ", MapStreamer.INSTANCE)";

        } else {
            return "writeObject(" + name + ")";
        }
    }

    /** Returns if the given class is an implementation of Map that doesn't know about Streaming */
    protected static boolean isNaiveMap (Class<?> type)
    {
        return Map.class.isAssignableFrom(type) && !type.equals(StreamableHashMap.class);
    }

    /** Returns if the given class is an implementation of List that doesn't know about Streaming */
    protected static boolean isNaiveList (Class<?> type)
    {
        return List.class.isAssignableFrom(type) && !type.equals(StreamableArrayList.class);
    }

    public static String toActionScriptType (Class<?> type, boolean isField)
    {
        if (type.isArray() || isNaiveList(type)) {
            if (Byte.TYPE.equals(type.getComponentType())) {
                return "flash.utils.ByteArray";
            }
            if (isField) {
                return "com.threerings.io.TypedArray";
            }
            return "Array";
        } else if (isNaiveMap(type)) {
            return "com.threerings.util.Map";
        } else if (Integer.TYPE.equals(type) ||
            Byte.TYPE.equals(type) ||
            Short.TYPE.equals(type) ||
            Character.TYPE.equals(type)) {
            return "int";
        } else if (Float.TYPE.equals(type) ||
            Double.TYPE.equals(type)) {
            return "Number";
        } else if (Long.TYPE.equals(type)) {
            return "com.threerings.util.Long";
        } else if (Boolean.TYPE.equals(type)) {
            return "Boolean";
        } else {
            // inner classes are not supported by ActionScript so we _
            return type.getName().replaceAll("\\$", "_");
        }
    }

    public static File createActionScriptPath (File actionScriptRoot, Class<?> sclass)
    {
        // determine the path to the corresponding action script source file
        String path = toActionScriptType(sclass, false).replace(".", File.separator);
        return new File(actionScriptRoot, path + ".as");
    }

    public static boolean hasOmitAnnotation (Class<?> cclass)
    {

        // if we have an ActionScript(omit=true) annotation, skip this class
        do {
            ActionScript asa = cclass.getAnnotation(ActionScript.class);
            if (asa != null && asa.omit()) {
                // System.err.println("Skipping " + sclass.getName() + "...");
                return true;
            }
            cclass = cclass.getSuperclass();
        } while (cclass != null);
        return false;
    }

    protected static final Splitter DOT_SPLITTER = Splitter.on('.');
}
