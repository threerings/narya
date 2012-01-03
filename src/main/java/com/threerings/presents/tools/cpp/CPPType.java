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

package com.threerings.presents.tools.cpp;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.threerings.io.Streamable;

import com.threerings.presents.dobj.DSet;

public class CPPType
{
    public static final String JAVA_LIST_FIXED = "JAVA_LIST_NAME()";

    /** The full cpp type. */
    public final String type;

    /** The method on ObjectInputStream and ObjectOutputStream to interpret this type. */
    public final String interpreter;

    /** CPP code to cast from this object to its reader type. */
    public final String cast;

    /** If this is a fixed type and thereby doesn't need a type encoding on the wire. */
    public final String fixed;

    /** Another type embedded in this type, or null if it doesn't have an embedded type. */
    public final CPPType dependent;

    public final String representationImport;

    public final Class<?> rawType;

    public final boolean primitive;

    public CPPType (Type javaType)
    {
        if (javaType instanceof ParameterizedType) {
            rawType = (Class<?>)((ParameterizedType)javaType).getRawType();
        } else if (javaType instanceof TypeVariable<?>) {
            rawType = (Class<?>)((TypeVariable<?>)javaType).getBounds()[0];
        } else {
            rawType = (Class<?>)javaType;
        }

        if (rawType.equals(List.class)) {
            fixed = JAVA_LIST_FIXED;
        } else {
            fixed = null;
        }

        if (rawType.isArray()) {
            Class<?> componentType = rawType.getComponentType();
            dependent = new CPPType(componentType);
            type = "Shared< std::vector< " + dependent.type + " > >";
            interpreter = componentType.equals(Object.class) ||
                componentType.isPrimitive() ? "Field" : "Object";
            representationImport = null;

        } else {
            type = CPPUtil.getCPPType(javaType);
            interpreter = getCPPInterpreter(rawType);
            if (javaType instanceof ParameterizedType) {
                Type[] typeArguments = ((ParameterizedType)javaType).getActualTypeArguments();
                if (rawType.equals(Comparable.class)) {
                    dependent = new CPPType(Streamable.class);
                    representationImport = "presents/Streamable.h";
                } else if (rawType == List.class) {
                    Class<?> param;
                    if (typeArguments[0] instanceof ParameterizedType) {
                        param = (Class<?>)((ParameterizedType)typeArguments[0]).getRawType();
                    } else {
                        param = (Class<?>)typeArguments[0];
                    }
                    if (!Streamable.class.isAssignableFrom(param)) {
                        throw new IllegalArgumentException(
                            "Lists may only contain Streamables in C++, not '" + typeArguments[0]
                                + "'");
                    }
                    dependent = new CPPType(param);
                    representationImport = null;
                } else if (rawType.equals(DSet.class)) {
                    dependent = null;
                    representationImport = CPPUtil.makePath(rawType, ".h");
                } else {
                    throw new IllegalArgumentException("Don't know how to handle " + rawType);
                }
            } else if (javaType instanceof TypeVariable<?>
                || javaType.equals(com.threerings.presents.dobj.DSet.Entry.class)) {
                dependent = new CPPType(Streamable.class);
                representationImport = "presents/Streamable.h";
            } else {
                if (!Streamable.class.equals(rawType) && Streamable.class.isAssignableFrom(rawType)) {
                    representationImport = CPPUtil.makePath(rawType, ".h");
                } else {
                    representationImport = null;
                }
                dependent = null;
            }
        }

        primitive = rawType.isPrimitive();
        Matcher m = TYPE_EXTRACT.matcher(type);
        cast = m.matches() ? "boost::static_pointer_cast<" + m.group(1) + ">" : type;
    }

    protected String getCPPInterpreter (Class<?> ftype)
    {
        if (ftype.equals(String.class) || ftype.equals(List.class)) {
            return "Field";
        } else if (ftype.equals(Boolean.TYPE)) {
            return "Boolean";
        } else if (ftype.equals(Byte.TYPE)) {
            return "Byte";
        } else if (ftype.equals(Short.TYPE)) {
            return "Short";
        } else if (ftype.equals(Integer.TYPE)) {
            return "Int";
        } else if (ftype.equals(Long.TYPE)) {
            return "Long";
        } else if (ftype.equals(Float.TYPE)) {
            return "Float";
        } else if (ftype.equals(Double.TYPE)) {
            return "Double";
        } else {
            return "Object";
        }
    }

    public String getCastFromStreamable (String name)
    {
        if (primitive) {
            return "boost::static_pointer_cast<presents::box::Boxed" + interpreter + ">(" + name + ")->value";
        }
        return cast + "(" + name + ")";
    }

    public String getAsStreamable (String name)
    {
        if (primitive) {
            return "presents::box::Boxed" + interpreter + "::createShared(" + name + ")";
        }
        return name;
    }

    public String getWithoutShared ()
    {
        Matcher m = TYPE_EXTRACT.matcher(type);
        return m.matches() ? m.group(1) : type;
    }

    protected static final Pattern TYPE_EXTRACT = Pattern.compile("Shared<(.*)>");
}
