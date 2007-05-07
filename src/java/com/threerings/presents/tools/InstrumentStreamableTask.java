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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.CannotCompileException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import com.samskivert.io.StreamUtil;
import com.threerings.io.FieldMarshaller;
import com.threerings.io.Streamable;

/**
 * Instruments compiled {@link Streamable} classes with public methods that can be used to stream
 * protected and private members when running in a sandboxed JVM.
 */
public class InstrumentStreamableTask extends Task
{
    /**
     * Adds a nested &lt;fileset&gt; element which enumerates streamable class files.
     */
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }

    /**
     * Adds a &lt;path&gt; element which defines our classpath.
     */
    public void addPath (Path path)
    {
        _paths.add(path);
    }

    /**
     * Configures the directory into which we write our instrumented class files.
     */
    public void setOutdir (File outdir)
    {
        _outdir = outdir;
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
        // configure our ClassPool with our classpath
        for (Path path : _paths) {
            for (String element : path.list()) {
                try {
                    _pool.appendClassPath(element);
                } catch (NotFoundException nfe) {
                    System.err.println("Invalid classpath entry [path=" + element + "]: " + nfe);
                }
            }
        }

        // now process the files
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
     * Processes a class file.
     */
    protected void processClass (File source)
    {
        CtClass clazz;
        InputStream in = null;
        try {
            clazz = _pool.makeClass(in = new BufferedInputStream(new FileInputStream(source)));
        } catch (IOException ioe) {
            System.err.println("Failed to load " + source + ": " + ioe);
            return;
        } finally {
            StreamUtil.close(in);
        }

        try {
            if (isStreamable(clazz)) {
                processStreamable(source, clazz);
            }
        } catch (NotFoundException nfe) {
            System.err.println("Error processing class [class=" + clazz.getName() +
                               ", error=" + nfe + "].");
        }
    }

    /**
     * Returns true if the supplied class or any of its parents implements {@link Streamable}.
     */
    protected boolean isStreamable (CtClass clazz)
        throws NotFoundException
    {
        if (clazz == null) {
            return false;
        }

        for (CtClass iface : clazz.getInterfaces()) {
            if (iface.getName().equals(Streamable.class.getName())) {
                return true;
            }
        }
        return isStreamable(clazz.getSuperclass());
    }

    /**
     * Instruments the supplied {@link Streamable} implementing class.
     */
    protected void processStreamable (File source, CtClass clazz)
        throws NotFoundException
    {
        ArrayList<CtField> fields = new ArrayList<CtField>();
        for (CtField field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) ||
                !(Modifier.isProtected(modifiers) || Modifier.isPrivate(modifiers))) {
                continue;
            }
            fields.add(field);
        }

        HashSet<String> methods = new HashSet<String>();
        for (CtMethod method : clazz.getMethods()) {
            methods.add(method.getName());
        }

        int added = 0;
        for (CtField field : fields) {
            String rname = FieldMarshaller.getReaderMethodName(field.getName());
            if (!methods.contains(rname)) {
                String reader =
                    "public void " + rname + " (com.threerings.io.ObjectInputStream ins) {\n" +
                    "    " + field.getName() + " = " + getFieldReader(field) + ";\n" +
                    "}";
                // System.out.println("Adding reader " + clazz.getName() + "." + rname);
                try {
                    clazz.addMethod(CtNewMethod.make(reader, clazz));
                    added++;
                } catch (CannotCompileException cce) {
                    System.err.println("Unable to compile reader [class=" + clazz.getName() +
                                       ", error=" + cce + "]:");
                    System.err.println(reader);
                }
            }

            String wname = FieldMarshaller.getWriterMethodName(field.getName());
            if (!methods.contains(wname)) {
                // System.out.println("Adding writer " + clazz.getName() + "." + wname);
                String writer =
                    "public void " + wname + " (com.threerings.io.ObjectOutputStream out) {\n" +
                    "    out." + getFieldWriter(field) + ";\n" +
                    "}";
                try {
                    clazz.addMethod(CtNewMethod.make(writer, clazz));
                    added++;
                } catch (CannotCompileException cce) {
                    System.err.println("Unable to compile writer [class=" + clazz.getName() +
                                       ", error=" + cce + "]:");
                    System.err.println(writer);
                }
            }
        }

        if (added > 0) {
            try {
                System.out.println("Instrumented '" + clazz.getName() + "'.");
                clazz.writeFile(_outdir.getPath());
            } catch (Exception e) {
                System.err.println("Failed to write instrumented class [class=" + clazz +
                                   ", outdir=" + _outdir + "]: " + e);
            }
        }
    }

    protected String getFieldReader (CtField field)
        throws NotFoundException
    {
        CtClass type = field.getType();
        if (type.getName().equals("java.lang.String")) {
            return "ins.readUTF()";
        } else if (type.equals(CtClass.booleanType) || type.getName().equals("java.lang.Boolean")) {
            return "ins.readBoolean()";
        } else if (type.equals(CtClass.byteType) || type.getName().equals("java.lang.Byte")) {
            return "ins.readByte()";
        } else if (type.equals(CtClass.shortType) || type.getName().equals("java.lang.Short")) {
            return "ins.readShort()";
        } else if (type.equals(CtClass.intType) || type.getName().equals("java.lang.Integer")) {
            return "ins.readInt()";
        } else if (type.equals(CtClass.longType) || type.getName().equals("java.lang.Long")) {
            return "ins.readLong()";
        } else if (type.equals(CtClass.floatType) || type.getName().equals("java.lang.Float")) {
            return "ins.readFloat()";
        } else if (type.equals(CtClass.doubleType) || type.getName().equals("java.lang.Double")) {
            return "ins.readDouble()";
        } else {
            return "(" + type.getSimpleName() + ")ins.readObject()";
        }
    }

    protected String getFieldWriter (CtField field)
        throws NotFoundException
    {
        CtClass type = field.getType();
        String name = field.getName();
        if (type.equals(CtClass.booleanType) || type.getName().equals("java.lang.Boolean")) {
            return "writeBoolean(" + name + ")";
        } else if (type.equals(CtClass.byteType) || type.getName().equals("java.lang.Byte")) {
            return "writeByte(" + name + ")";
        } else if (type.equals(CtClass.shortType) || type.getName().equals("java.lang.Short")) {
            return "writeShort(" + name + ")";
        } else if (type.equals(CtClass.intType) || type.getName().equals("java.lang.Integer")) {
            return "writeInt(" + name + ")";
        } else if (type.equals(CtClass.longType) || type.getName().equals("java.lang.Long")) {
            return "writeLong(" + name + ")";
        } else if (type.equals(CtClass.floatType) || type.getName().equals("java.lang.Float")) {
            return "writeFloat(" + name + ")";
        } else if (type.equals(CtClass.doubleType) || type.getName().equals("java.lang.Double")) {
            return "writeDouble(" + name + ")";
        } else if (type.getName().equals("java.lang.String")) {
            return "writeUTF(" + name + ")";
        } else {
            return "writeObject(" + name + ")";
        }
    }

    /** A list of filesets that contain Streamable class files. */
    protected ArrayList<FileSet> _filesets = new ArrayList<FileSet>();

    /** A list of paths that make up our classpath. */
    protected ArrayList<Path> _paths = new ArrayList<Path>();

    /** The directory to which we write our instrumented class files. */
    protected File _outdir;

    /** Used to instrument class files. */
    protected ClassPool _pool = ClassPool.getDefault();
}
