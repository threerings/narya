//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.util.ActionScript;

import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.DObject;

/**
 * Generates ActionScript versions of {@link Streamable} classes and provides routines used by the
 * {@link GenDObjectTask} to create ActionScript versions of distributed objects.
 */
public class GenActionScriptTask extends GenTask
{
    /**
     * Configures the path to our ActionScript source files.
     */
    public void setAsroot (File asroot)
    {
        _asroot = asroot;
    }

    /**
     * Configures us with a header file that we'll prepend to all generated source files.
     */
    public void setHeader (File header)
    {
        try {
            _header = Files.toString(header, Charsets.UTF_8);
        } catch (IOException ioe) {
            System.err.println("Unabled to load header '" + header + ": " + ioe.getMessage());
        }
    }

    /**
     * Processes a resolved Streamable class instance.
     */
    @Override
    public void processClass (File source, Class<?> sclass)
        throws Exception
    {
        // make sure we implement Streamable but don't extend DObject or InvocationMarshaller and
        // that we're a class not an interface
        if (!Streamable.class.isAssignableFrom(sclass) ||
            DObject.class.isAssignableFrom(sclass) ||
            InvocationMarshaller.class.isAssignableFrom(sclass) ||
            ((sclass.getModifiers() & Modifier.INTERFACE) != 0)) {
            // System.err.println("Skipping " + sclass.getName() + "...");
            return;
        }

        // if we have an ActionScript(omit=true) annotation, skip this class
        Class<?> cclass = sclass;
        do {
            ActionScript asa = cclass.getAnnotation(ActionScript.class);
            if (asa != null && asa.omit()) {
                // System.err.println("Skipping " + sclass.getName() + "...");
                return;
            }
            cclass = cclass.getSuperclass();
        } while (cclass != null);

        // determine the path to the corresponding action script source file
        String path = toActionScriptType(sclass, false).replace(".", File.separator);
        File asfile = new File(_asroot, path + ".as");

        System.err.println("Converting " + sclass.getName() + "...");
        convert(source, sclass, asfile);
    }

    protected void convert (File javaSource, Class<?> sclass, File output)
        throws Exception
    {
        // parse the existing ActionScript source and generate what we don't
        // have from the Java class
        ActionScriptSource assrc = new ActionScriptSource(sclass);
        assrc.absorbJava(javaSource);
        assrc.imports.add(ObjectInputStream.class.getName());
        assrc.imports.add(ObjectOutputStream.class.getName());

        // see if our parent also implements Streamable
        boolean needSuper = Streamable.class.isAssignableFrom(sclass.getSuperclass());

        // add readObject() and writeObject() definitions
        ActionScriptSource.Member member;
        member = new ActionScriptSource.Member(
            "readObject", (needSuper ? "override " : "") + READ_SIG);
        member.noreplace = true;
        member.comment = "    // from interface Streamable\n";
        StringBuilder body = new StringBuilder("    {\n");
        if (needSuper) {
            body.append("        super.readObject(ins);\n");
        }
        int added = 0;
        for (Field field : sclass.getDeclaredFields()) {
            if (!isStreamable(field)) {
                continue;
            }
            body.append("        ");
            body.append(field.getName()).append(" = ins.");
            body.append(toReadObject(field.getType()));
            body.append(";\n");
            added++;
        }
        member.body = body.append("    }\n").toString();
        if (added > 0) {
            assrc.publicMethods.add(member);
        }

        member = new ActionScriptSource.Member(
            "writeObject", (needSuper ? "override " : "") + WRITE_SIG);
        member.noreplace = true;
        member.comment = "    // from interface Streamable\n";
        body = new StringBuilder("    {\n");
        if (needSuper) {
            body.append("        super.writeObject(out);\n");
        }
        added = 0;
        for (Field field : sclass.getDeclaredFields()) {
            if (!isStreamable(field)) {
                continue;
            }
            body.append("        out.");
            body.append(toWriteObject(field.getType(), field.getName()));
            body.append(";\n");
            added++;
        }
        member.body = body.append("    }\n").toString();
        if (added > 0) {
            assrc.publicMethods.add(member);
        }

        // now we can parse existing definitions from any extant ActionScript source file
        assrc.absorbActionScript(output);

        // make sure our parent directory exists
        output.getParentFile().mkdirs();

        // now write all that out to the target source file
        BufferedWriter out = new BufferedWriter(new FileWriter(output));
        assrc.write(new PrintWriter(out));
    }

    protected boolean isStreamable (Field field)
    {
        int mods = field.getModifiers();
        return !Modifier.isStatic(mods) && !Modifier.isTransient(mods);
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

    protected static boolean needsActionScriptImport (Class<?> type, boolean isField)
    {
        if (type.isArray()) {
            return Byte.TYPE.equals(type.getComponentType()) || isField;
        }
        return (Long.TYPE.equals(type) || !type.isPrimitive()) && !String.class.equals(type);
    }

    protected static String toActionScriptType (Class<?> type, boolean isField)
    {
        if (type.isArray() || List.class.isAssignableFrom(type)) {
            if (Byte.TYPE.equals(type.getComponentType())) {
                return "flash.utils.ByteArray";
            }
            if (isField) {
                return "com.threerings.io.TypedArray";
            }
            return "Array";
        } else if (Map.class.isAssignableFrom(type)) {
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

        } else if (Map.class.isAssignableFrom(type)) {
            return "readField(MapStreamer.INSTANCE)";

        } else if (List.class.isAssignableFrom(type)) {
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

        } else if (List.class.isAssignableFrom(type)) {
            return "writeField(" + name + ", ArrayStreamer.INSTANCE)";

        } else if (Map.class.isAssignableFrom(type)) {
            return "writeField(" + name + ", MapStreamer.INSTANCE)";

        } else {
            return "writeObject(" + name + ")";
        }
    }

    /** A header to put on all generated source files. */
    protected String _header;

    /** The path to our ActionScript source files. */
    protected File _asroot;

    protected static final String READ_SIG =
        "public function readObject (ins :ObjectInputStream) :void";
    protected static final String WRITE_SIG =
        "public function writeObject (out :ObjectOutputStream) :void";

    protected static final Splitter DOT_SPLITTER = Splitter.on('.');

}
