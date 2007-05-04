//
// $Id: GenStremableTask.java 4672 2007-04-18 16:40:57Z mdb $
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

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.threerings.io.Streamable;

import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.DObject;

/**
 * Generates <code>readObject()</code> and <code>writeObject()</code> methods for {@link
 * Streamable} classes that have protected or private members so that they can be used in a
 * sandboxed environment.
 */
public class GenStreamableTask extends Task
{
    /**
     * Adds a nested &lt;fileset&gt; element which enumerates streamable source files.
     */
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
        for (FileSet fs : _filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (int f = 0; f < srcFiles.length; f++) {
                processClass(new File(fromDir, srcFiles[f]));
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
    protected void processClass (File source, Class<?> sclass)
        throws IOException
    {
        // we must implement Streamable, not be a DObject and not be an interface ourselves
        if (!Streamable.class.isAssignableFrom(sclass) || DObject.class.isAssignableFrom(sclass) ||
            Modifier.isInterface(sclass.getModifiers())) {
            // System.err.println("Skipping " + sclass.getName() + "...");
            return;
        }

        // add readObject() and writeObject() definitions
        StringBuffer readbuf = new StringBuffer(READ_OPEN);
        StringBuffer writebuf = new StringBuffer(WRITE_OPEN);

        // see if our parent also implements Streamable
        Class supster = sclass.getSuperclass();
        do {
            if (isStreamable(supster)) {
                readbuf.append("        super.readObject(ins);\n");
                writebuf.append("        super.writeObject(out);\n");
                break;
            }
            supster = supster.getSuperclass();
        } while (supster != null);

        int added = 0;
        for (Field field : sclass.getDeclaredFields()) {
            if (!isStreamableField(field)) {
                continue;
            }
            readbuf.append("        ");
            readbuf.append(field.getName()).append(" = ");
            readbuf.append(toReadObject(field));
            readbuf.append(";\n");

            writebuf.append("        out.");
            writebuf.append(toWriteObject(field));
            writebuf.append(";\n");
            added++;
        }
        if (added == 0) {
            return; // nothing to do
        }

        readbuf.append(READ_CLOSE);
        writebuf.append(WRITE_CLOSE);

        System.err.println("Converting " + sclass.getName() + "...");

        SourceFile sfile = new SourceFile();
        try {
            sfile.readFrom(source);
            // don't overwrite an existing readObject() or writeObject()
            String readsec =
                sfile.containsString("public void readObject") ? "" : readbuf.toString();
            String writesec =
                sfile.containsString("public void writeObject") ? "" : writebuf.toString();
            sfile.writeTo(source, readsec, writesec);
        } catch (IOException ioe) {
            System.err.println("Error processing " + source + ": " + ioe);
        }
    }

    protected boolean isStreamable (Class clazz)
    {
        for (Class iface : clazz.getInterfaces()) {
            if (Streamable.class.equals(iface)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isStreamableField (Field field)
    {
        int mods = field.getModifiers();
        return !Modifier.isStatic(mods) && !Modifier.isTransient(mods);
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
    protected ArrayList<FileSet> _filesets = new ArrayList<FileSet>();

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
