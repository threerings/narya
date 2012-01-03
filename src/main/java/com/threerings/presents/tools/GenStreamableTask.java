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

import java.lang.reflect.Field;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import com.google.common.collect.Lists;

import com.threerings.io.Streamable;

import com.threerings.presents.dobj.DObject;

/**
 * Generates <code>readObject()</code> and <code>writeObject()</code> methods for {@link
 * Streamable} classes that have protected or private members so that they can be used in a
 * sandboxed environment.
 */
public class GenStreamableTask extends GenTask
{
    /**
     * Adds a nested &lt;fileset&gt; element which enumerates streamable source
     * files.
     */
    @Override
    public void addFileset(FileSet set) {
        _filesets.add(set);
    }

    @Override
    public void execute ()
    {
        for (FileSet fs : _filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (String srcFile : srcFiles) {
                processClass(new File(fromDir, srcFile));
            }
        }
    }

    /**
     * Processes a {@link Streamable} source file.
     */
    protected void processClass (File source)
    {
        // load up the file and determine it's package and classname
        String name = null;
        try {
            name = GenUtil.readClassName(source);
        } catch (Exception e) {
            System.err.println("Failed to parse " + source + ": " + e.getMessage());
            return;
        }

        System.err.println("Considering " + name + "...");

        try {
            // in order for annotations to work, this task and all the classes it uses must be
            // loaded from the same class loader as the classes on which we are going to
            // introspect; this is non-ideal but unavoidable
            processClass(source, getClass().getClassLoader().loadClass(name));
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Failed to load " + name + ".\nMissing class: " + cnfe.getMessage());
            System.err.println("Be sure to set the 'classpathref' attribute to a classpath\n" +
                               "that contains your projects invocation service classes.");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Processes a resolved {@link Streamable} class instance.
     */
    @Override
    protected void processClass (File source, Class<?> sclass)
        throws IOException
    {
        StreamableClassRequirements reqs = new StreamableClassRequirements(sclass);
        // we must implement Streamable, not be a DObject and have some fields that need to be
        // streamed
        if (!Streamable.class.isAssignableFrom(sclass) || DObject.class.isAssignableFrom(sclass) ||
                reqs.streamedFields.isEmpty()) {
            // System.err.println("Skipping " + sclass.getName() + "...");
            return;
        }

        // add readObject() and writeObject() definitions
        StringBuilder readbuf = new StringBuilder(READ_OPEN);
        StringBuilder writebuf = new StringBuilder(WRITE_OPEN);

        if (reqs.superclassStreamable) {
            readbuf.append("        super.readObject(ins);\n");
            writebuf.append("        super.writeObject(out);\n");
        }
        for (Field field : reqs.streamedFields) {
            readbuf.append("        ");
            readbuf.append(field.getName()).append(" = ");
            readbuf.append(toReadObject(field));
            readbuf.append(";\n");

            writebuf.append("        out.");
            writebuf.append(toWriteObject(field));
            writebuf.append(";\n");
        }

        readbuf.append(READ_CLOSE);
        writebuf.append(WRITE_CLOSE);

        SourceFile sfile = new SourceFile();
        try {
            sfile.readFrom(source);
        } catch (IOException ioe) {
            System.err.println("Error reading " + source + ": " + ioe);
        }

        // don't overwrite an existing readObject() or writeObject()
        StringBuilder methods = new StringBuilder();
        if (!sfile.containsString("public void readObject")) {
            methods.append(readbuf);
        }
        if (!sfile.containsString("public void writeObject")) {
            if (methods.length() > 0) {
                methods.append("\n");
            }
            methods.append(writebuf);
        }
        if (methods.length() == 0) {
            return; // nothing to do
        }

        System.err.println("Converting " + sclass.getName() + "...");

        writeFile(source.getAbsolutePath(), sfile.generate(null, methods.toString()));
    }

    protected String toReadObject (Field field)
    {
        Class<?> type = field.getType();
        if (type.equals(String.class)) {
            return "ins.readUTF()";
        } else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return "ins.readBoolean()";
        } else if (type.equals(Byte.TYPE) || type.equals(Byte.class)) {
            return "ins.readByte()";
        } else if (type.equals(Short.TYPE) || type.equals(Short.class)) {
            return "ins.readShort()";
        } else if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return "ins.readInt()";
        } else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
            return "ins.readLong()";
        } else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
            return "ins.readFloat()";
        } else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
            return "ins.readDouble()";
        } else {
            return "(" + GenUtil.simpleName(field) + ")ins.readObject()";
        }
    }

    protected String toWriteObject (Field field)
    {
        Class<?> type = field.getType();
        String name = field.getName();
        if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return "writeBoolean(" + name + ")";
        } else if (type.equals(Byte.TYPE) || type.equals(Byte.class)) {
            return "writeByte(" + name + ")";
        } else if (type.equals(Short.TYPE) || type.equals(Short.class)) {
            return "writeShort(" + name + ")";
        } else if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return "writeInt(" + name + ")";
        } else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
            return "writeLong(" + name + ")";
        } else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
            return "writeFloat(" + name + ")";
        } else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
            return "writeDouble(" + name + ")";
        } else if (type.equals(String.class)) {
            return "writeUTF(" + name + ")";
        } else {
            return "writeObject(" + name + ")";
        }
    }

    /** A list of filesets that contain tile images. */
    protected ArrayList<FileSet> _filesets = Lists.newArrayList();

    protected static final String READ_OPEN =
        "    // from interface Streamable\n" +
        "    public void readObject (ObjectInputStream ins)\n" +
        "        throws IOException, ClassNotFoundException\n" +
        "    {\n";
    protected static final String READ_CLOSE = "    }\n";

    protected static final String WRITE_OPEN =
        "    // from interface Streamable\n" +
        "    public void writeObject (ObjectOutputStream out)\n" +
        "        throws IOException\n" +
        "    {\n";
    protected static final String WRITE_CLOSE = "    }\n";
}
