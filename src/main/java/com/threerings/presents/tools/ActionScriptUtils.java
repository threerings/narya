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

package com.threerings.presents.tools;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import com.threerings.util.ActionScript;
import com.threerings.util.StreamableArrayList;
import com.threerings.util.StreamableHashMap;
import com.threerings.util.StreamableHashSet;

public class ActionScriptUtils
{
    /**
     * Adds an existing ActionScript file's imports to the given ImportSet.
     * @param asFile a String containing the contents of an ActionScript file
     */
    public static void addExistingImports (String asFile, ImportSet imports)
    {
        // Discover the location of the 'public class' declaration.
        // We won't search past there.
        Matcher m = AS_PUBLIC_CLASS_DECL.matcher(asFile);
        int searchTo = asFile.length();
        if (m.find()) {
            searchTo = m.start();
        }

        m = AS_IMPORT.matcher(asFile.substring(0, searchTo));
        while (m.find()) {
            imports.add(m.group(3));
        }
    }

    public static String addImportAndGetShortType (Class<?> type, boolean isField,
        ImportSet imports)
    {
        String full = toActionScriptType(type, isField);
        if (needsActionScriptImport(type, isField)) {
            imports.add(full);
        }
        return Iterables.getLast(DOT_SPLITTER.split(full));
    }

    public static String toSimpleName (Class<?> type)
    {
        String name = type.getName().substring(type.getName().lastIndexOf(".") + 1);
        // inner classes are not supported by ActionScript so we _
        return name.replaceAll("\\$", "_");
    }

    public static String toReadObject (Class<?> type)
    {
        if (type.equals(String.class)) {
            return "readField(String)";

        } else if (type.equals(Integer.class) ||
                   type.equals(Short.class) ||
                   type.equals(Byte.class)) {
            return "readField(" + toSimpleName(type) + ").value";

        } else if (type.equals(Long.class)) {
            return "readField(" + toSimpleName(type) + ")";

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

        } else if (isNaiveSet(type)) {
            return "readField(SetStreamer.INSTANCE)";

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

        } else if (isNaiveSet(type)) {
            return "writeField(" + name + ", SetStreamer.INSTANCE)";

        } else {
            return "writeObject(" + name + ")";
        }
    }

    public static String toActionScriptType (Class<?> type, boolean isField)
    {
        if (type.isArray() || isNaiveList(type)) {
            if (Byte.TYPE.equals(type.getComponentType())) {
                return "flash.utils.ByteArray";
            } else if (isField) {
                return "com.threerings.io.TypedArray";
            } else {
                return "Array";
            }
        } else if (isNaiveMap(type)) {
            return "com.threerings.util.Map";
        } else if (isNaiveSet(type)) {
            return "com.threerings.util.Set";
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
        } else if (Cloneable.class.equals(type)) {
            return "com.threerings.util.Cloneable";
        } else if (Comparable.class.equals(type)) {
            return "com.threerings.util.Comparable";
        } else {
            // inner classes are not supported by ActionScript so we _
            return type.getName().replaceAll("\\$", "_");
        }
    }

    /**
     * Converts java types to their actionscript equivalents for ooo-style streaming.
     */
    public static void convertBaseClasses (ImportSet imports)
    {
        // replace primitive types with OOO types (required for unboxing)
        imports.replace("byte", "com.threerings.util.Byte");
        imports.replace("boolean", "com.threerings.util.langBoolean");
        imports.replace("[B", "flash.utils.ByteArray");
        imports.replace("float", "com.threerings.util.Float");
        imports.replace("long", "com.threerings.util.Long");

        if (imports.removeAll("[*") > 0) {
            imports.add("com.threerings.io.TypedArray");
        }

        // convert java primitive boxes to their ooo counterparts
        imports.replace(Integer.class, "com.threerings.util.Integer");

        // convert some java.util types to their ooo counterparts
        imports.replace(Map.class, "com.threerings.util.Map");

        // get rid of java.lang stuff and any remaining primitives
        imports.removeGlobals();

        // get rid of remaining arrays
        imports.removeArrays();
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

    /** Returns if the given class is an implementation of Map that doesn't know about Streaming */
    protected static boolean isNaiveMap (Class<?> type)
    {
        return Map.class.isAssignableFrom(type) && !type.equals(StreamableHashMap.class);
    }

    protected static boolean isNaiveSet (Class<?> type)
    {
        return Set.class.isAssignableFrom(type) && !type.equals(StreamableHashSet.class);
    }

    /** Returns if the given class is an implementation of List that doesn't know about Streaming */
    protected static boolean isNaiveList (Class<?> type)
    {
        return List.class.isAssignableFrom(type) && !type.equals(StreamableArrayList.class);
    }

    protected static boolean needsActionScriptImport (Class<?> type, boolean isField)
    {
        if (type.isArray()) {
            return Byte.TYPE.equals(type.getComponentType()) || isField;
        }
        return (Long.TYPE.equals(type) || !type.isPrimitive()) && !String.class.equals(type);
    }

    protected static final Splitter DOT_SPLITTER = Splitter.on('.');

    protected static final Pattern AS_PUBLIC_CLASS_DECL = Pattern.compile("^public class(.*)$",
        Pattern.MULTILINE);
    protected static final Pattern AS_IMPORT = Pattern.compile("^(\\s*)import(\\s+)([^;]*);",
        Pattern.MULTILINE);
}
