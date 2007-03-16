//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.samskivert.util.StringUtil;

/**
 * Utility methods used by our various source code generating tasks.
 */
public class GenUtil extends com.samskivert.util.GenUtil
{
    /** A regular expression for matching the package declaration. */
    public static final Pattern PACKAGE_PATTERN =
        Pattern.compile("^\\s*package\\s+(\\S+)\\W");

    /** A regular expression for matching the class or interface
     * declaration. */
    public static final Pattern NAME_PATTERN =
        Pattern.compile("^\\s*public\\s+(?:abstract\\s+)?" +
                        "(interface|class)\\s+(\\S+)(\\W|$)");

    /**
     * Returns the name of the supplied class as it would appear in
     * ActionScript code using the class (no package prefix, arrays specified
     * as Array<code>Array</code>).
     */
    public static String simpleASName (Class<?> clazz)
    {
        if (clazz.isArray()) {
            if (Byte.TYPE.equals(clazz.getComponentType())) {
                return "ByteArray";
            }
            return "Array";
        } else if (clazz == Boolean.TYPE) {
            return "Boolean";
        } else if (clazz == Byte.TYPE || clazz == Character.TYPE ||
                   clazz == Short.TYPE || clazz == Integer.TYPE) {
            return "int";
        } else if (clazz == Long.TYPE) {
            return "Long";
        } else if (clazz == Float.TYPE || clazz == Double.TYPE) {
            return "Number";
        } else {
            String cname = clazz.getName();
            Package pkg = clazz.getPackage();
            int offset = (pkg == null) ? 0 : pkg.getName().length()+1;
            String name = cname.substring(offset);
            return StringUtil.replace(name, "$", "_");
        }
    }

    /**
     * "Boxes" the supplied argument, ie. turning an <code>int</code> into
     * an <code>Integer</code> object.
     */
    public static String boxArgument (Class<?> clazz, String name)
    {
        if (clazz == Boolean.TYPE) {
            return "Boolean.valueOf(" + name + ")";
        } else if (clazz == Byte.TYPE) {
            return "Byte.valueOf(" + name + ")";
        } else if (clazz == Character.TYPE) {
            return "Character.valueOf(" + name + ")";
        } else if (clazz == Short.TYPE) {
            return "Short.valueOf(" + name + ")";
        } else if (clazz == Integer.TYPE) {
            return "Integer.valueOf(" + name + ")";
        } else if (clazz == Long.TYPE) {
            return "Long.valueOf(" + name + ")";
        } else if (clazz == Float.TYPE) {
            return "Float.valueOf(" + name + ")";
        } else if (clazz == Double.TYPE) {
            return "Double.valueOf(" + name + ")";
        } else {
            return name;
        }
    }

    /**
     * "Unboxes" the supplied argument, ie. turning an <code>Integer</code>
     * object into an <code>int</code>.
     */
    public static String unboxArgument (Class<?> clazz, Type type, String name)
    {
        if (clazz == Boolean.TYPE) {
            return "((Boolean)" + name + ").booleanValue()";
        } else if (clazz == Byte.TYPE) {
            return "((Byte)" + name + ").byteValue()";
        } else if (clazz == Character.TYPE) {
            return "((Character)" + name + ").charValue()";
        } else if (clazz == Short.TYPE) {
            return "((Short)" + name + ").shortValue()";
        } else if (clazz == Integer.TYPE) {
            return "((Integer)" + name + ").intValue()";
        } else if (clazz == Long.TYPE) {
            return "((Long)" + name + ").longValue()";
        } else if (clazz == Float.TYPE) {
            return "((Float)" + name + ").floatValue()";
        } else if (clazz == Double.TYPE) {
            return "((Double)" + name + ").doubleValue()";
        } else {
            return "(" + simpleName(clazz, type) + ")" + name + "";
        }
    }

    /**
     * "Boxes" the supplied argument, ie. turning an <code>int</code> into
     * an <code>Integer</code> object.
     */
    public static String boxASArgument (Class<?> clazz, String name)
    {
        if (clazz == Boolean.TYPE) {
            return "langBoolean.valueOf(" + name + ")";
        } else if (clazz == Byte.TYPE) {
            return "Byte.valueOf(" + name + ")";
        } else if (clazz == Character.TYPE) {
            return "Character.valueOf(" + name + ")";
        } else if (clazz == Short.TYPE) {
            return "Short.valueOf(" + name + ")";
        } else if (clazz == Integer.TYPE) {
            return "Integer.valueOf(" + name + ")";
        } else if (clazz == Long.TYPE) {
            return name; // Long is left as is
        } else if (clazz == Float.TYPE) {
            return "Float.valueOf(" + name + ")";
        } else if (clazz == Double.TYPE) {
            return "Double.valueOf(" + name + ")";
        } else {
            return name;
        }
    }

    /**
     * "Unboxes" the supplied argument, ie. turning an <code>Integer</code>
     * object into an <code>int</code>.
     */
    public static String unboxASArgument (Class<?> clazz, String name)
    {
        if (clazz == Boolean.TYPE) {
            return "(" + name + " as Boolean)";
        } else if (clazz == Byte.TYPE ||
                   clazz == Character.TYPE ||
                   clazz == Short.TYPE ||
                   clazz == Integer.TYPE) {
            return "(" + name + " as int)";
        } else if (clazz == Long.TYPE) {
            return "(" + name + " as Long)";
        } else if (clazz == Float.TYPE ||
                   clazz == Double.TYPE) {
            return "(" + name + " as Number)";
        } else {
            return "(" + name + " as " + simpleASName(clazz) + ")";
        }
    }

    /**
     * Potentially clones the supplied argument if it is the type that
     * needs such treatment.
     */
    public static String cloneArgument (Class<?> dsclazz, Field f, String name)
    {
        Class<?> clazz = f.getType();
        if (dsclazz.equals(clazz)) {
            return "(" + name + " == null) ? null : " + name + ".typedClone()";
        } else if (clazz.isArray() || dsclazz.isAssignableFrom(clazz)) {
            return "(" + name + " == null) ? null : " +
                "(" + simpleName(f) + ")" + name + ".clone()";
        } else {
            return name;
        }
    }

    /**
     * Reads in the supplied source file and locates the package and class
     * or interface name and returns a fully qualified class name.
     */
    public static String readClassName (File source)
        throws IOException
    {
        // load up the file and determine it's package and classname
        String pkgname = null, name = null;
        BufferedReader bin = new BufferedReader(new FileReader(source));
        String line;
        while ((line = bin.readLine()) != null) {
            Matcher pm = PACKAGE_PATTERN.matcher(line);
            if (pm.find()) {
                pkgname = pm.group(1);
            }
            Matcher nm = NAME_PATTERN.matcher(line);
            if (nm.find()) {
                name = nm.group(2);
                break;
            }
        }
        bin.close();

        // make sure we found something
        if (name == null) {
            throw new IOException(
                "Unable to locate class or interface name in " + source + ".");
        }

        // prepend the package name to get a name we can Class.forName()
        if (pkgname != null) {
            name = pkgname + "." + name;
        }

        return name;
    }
}
