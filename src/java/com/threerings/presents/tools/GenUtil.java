//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.samskivert.util.StringUtil;

/**
 * Utility methods used by our various source code generating tasks.
 */
public class GenUtil
{
    /** A regular expression for matching the package declaration. */
    public static final Pattern PACKAGE_PATTERN =
        Pattern.compile("^\\s*package\\s+(\\S+)\\W");

    /** A regular expression for matching the class or interface
     * declaration. */
    public static final Pattern NAME_PATTERN =
        Pattern.compile("^\\s*public\\s+(interface|class)\\s+(\\S+)(\\W|$)");

    /**
     * Returns the name of the supplied class as it would likely appear in
     * code using the class (no package prefix, arrays specified as
     * <code>type[]</code>).
     */
    public static String simpleName (Class clazz)
    {
        if (clazz.isArray()) {
            return simpleName(clazz.getComponentType()) + "[]";
        } else {
            Package pkg = clazz.getPackage();
            int offset = (pkg == null) ? 0 : pkg.getName().length()+1;
            String name = clazz.getName().substring(offset);
            return StringUtil.replace(name, "$", ".");
        }
    }

    /**
     * "Boxes" the supplied argument, ie. turning an <code>int</code> into
     * an <code>Integer</code> object.
     */
    public static String boxArgument (Class clazz, String name)
    {
        if (clazz == Boolean.TYPE) {
            return "new Boolean(" + name + ")";
        } else if (clazz == Byte.TYPE) {
            return "new Byte(" + name + ")";
        } else if (clazz == Character.TYPE) {
            return "new Character(" + name + ")";
        } else if (clazz == Short.TYPE) {
            return "new Short(" + name + ")";
        } else if (clazz == Integer.TYPE) {
            return "new Integer(" + name + ")";
        } else if (clazz == Long.TYPE) {
            return "new Long(" + name + ")";
        } else if (clazz == Float.TYPE) {
            return "new Float(" + name + ")";
        } else if (clazz == Double.TYPE) {
            return "new Double(" + name + ")";
        } else {
            return name;
        }
    }

    /**
     * "Unboxes" the supplied argument, ie. turning an
     * <code>Integer</code> object into an <code>int</code>.
     */
    public static String unboxArgument (Class clazz, String name)
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
            return "(" + simpleName(clazz) + ")" + name + "";
        }
    }

    /**
     * Potentially clones the supplied argument if it is the type that
     * needs such treatment.
     */
    public static String cloneArgument (Class dsclazz, Class clazz, String name)
    {
        if (clazz.isArray() || dsclazz.isAssignableFrom(clazz)) {
            return "(" + name + " == null) ? null : " +
                "(" + simpleName(clazz) + ")" + name + ".clone()";
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
