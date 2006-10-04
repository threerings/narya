//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ClasspathUtils;

import org.apache.commons.io.IOUtils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.samskivert.util.StringUtil;
import com.samskivert.velocity.VelocityUtil;

import com.threerings.io.Streamable;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.DObject;

/**
 * Generates ActionScript versions of {@link Streamable} classes and provides
 * routines used by the {@link GenDObjectTask} to create ActionScript versions
 * of distributed objects.
 */
public class GenActionScriptTask extends Task
{
    /**
     * Adds a nested &lt;fileset&gt; element which enumerates streamable source
     * files.
     */
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }

    /**
     * Configures the path to our ActionScript source files.
     */
    public void setAsroot (File asroot)
    {
        _asroot = asroot;
    }

    /**
     * Configures us with a header file that we'll prepend to all
     * generated source files.
     */
    public void setHeader (File header)
    {
        try {
            _header = IOUtils.toString(new FileReader(header));
        } catch (IOException ioe) {
            System.err.println("Unabled to load header '" + header + ": " +
                               ioe.getMessage());
        }
    }

    /**
     * Configures the classpath that we'll use to load classes.
     */
    public void setClasspathref (Reference pathref)
    {
        _cloader = ClasspathUtils.getClassLoaderForPath(getProject(), pathref);
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
        if (_cloader == null) {
            String errmsg = "This task requires a 'classpathref' attribute " +
                "to be set to the project's classpath.";
            throw new BuildException(errmsg);
        }

        try {
            _velocity = VelocityUtil.createEngine();
        } catch (Exception e) {
            throw new BuildException("Failure initializing Velocity", e);
        }

        // resolve the Streamable class using our classloader
        try {
            _sclass = _cloader.loadClass(Streamable.class.getName());
            _doclass = _cloader.loadClass(DObject.class.getName());
            _imclass = _cloader.loadClass(InvocationMarshaller.class.getName());
        } catch (Exception e) {
            throw new BuildException("Can't resolve Streamable", e);
        }

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
     * Processes a Streamable source file.
     */
    protected void processClass (File source)
    {
        // System.err.println("Processing " + source + "...");

        // load up the file and determine it's package and classname
        String name = null;
        try {
            name = GenUtil.readClassName(source);
        } catch (Exception e) {
            System.err.println(
                "Failed to parse " + source + ": " + e.getMessage());
            return;
        }

        try {
            processClass(source, _cloader.loadClass(name));
        } catch (ClassNotFoundException cnfe) {
            System.err.println(
                "Failed to load " + name + ".\n" +
                "Missing class: " + cnfe.getMessage());
            System.err.println(
                "Be sure to set the 'classpathref' attribute to a classpath\n" +
                "that contains your projects invocation service classes.");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Processes a resolved Streamable class instance.
     */
    protected void processClass (File source, Class sclass)
        throws IOException
    {
        // make sure we implement Streamable but don't extend DObject or
        // InvocationMarshaller and that we're a class not an interface
        if (!_sclass.isAssignableFrom(sclass) ||
            _doclass.isAssignableFrom(sclass) ||
            _imclass.isAssignableFrom(sclass) ||
            ((sclass.getModifiers() & Modifier.INTERFACE) != 0)) {
            // System.err.println("Skipping " + sclass.getName() + "...");
            return;
        }

        // determine the path to the corresponding action script source file
        String path = sclass.getPackage().getName();
        path = path.replace(".", File.separator);
        String name = sclass.getName();
        name = name.substring(name.lastIndexOf(".")+1);
        path = path + File.separator + name;
        File asfile = new File(_asroot, path + ".as");

        System.err.println("Converting " + sclass.getName() + "...");

        // parse the existing ActionScript source and generate what we don't
        // have from the Java class
        ActionScriptSource assrc = new ActionScriptSource(sclass);
        assrc.absorbJava(source);

        // see if our parent also implements Streamable
        boolean needSuper = false;
        Class supster = sclass.getSuperclass();
        do {
            if (isStreamable(supster)) {
                needSuper = true;
                break;
            }
            supster = supster.getSuperclass();
        } while (supster != null);

        // add readObject() and writeObject() definitions
        ActionScriptSource.Member member;
        member = new ActionScriptSource.Member(
            "readObject", (needSuper ? "override " : "") + READ_SIG);
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
            body.append(field.getName()).append(" = ");
            body.append(toReadObject(field.getType().getName()));
            body.append(";\n");
            added++;
        }
        member.body = body.append("    }\n").toString();
        if (added > 0) {
            assrc.publicMethods.add(member);
        }

        member = new ActionScriptSource.Member(
            "writeObject", (needSuper ? "override " : "") + WRITE_SIG);
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
            body.append(
                toWriteObject(field.getType().getName(), field.getName()));
            body.append(";\n");
            added++;
        }
        member.body = body.append("    }\n").toString();
        if (added > 0) {
            assrc.publicMethods.add(member);
        }

        // now we can parse existing definitions from any extant ActionScript
        // source file
        assrc.absorbActionScript(asfile);

        // make sure our parent directory exists
        asfile.getParentFile().mkdirs();

        // now write all that out to the target source file
        BufferedWriter out = new BufferedWriter(new FileWriter(asfile));
        assrc.write(new PrintWriter(out));
    }

    protected boolean isStreamable (Class clazz)
    {
        for (Class iface : clazz.getInterfaces()) {
            if (_sclass.equals(iface)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isStreamable (Field field)
    {
        int mods = field.getModifiers();
        return !Modifier.isStatic(mods) && !Modifier.isTransient(mods);
    }

    protected String toReadObject (String type)
    {
        if (type.equals("java.lang.String")) {
            return "(ins.readField(String) as String)";

        } else if (type.equals("java.lang.Integer")) {
            type = ActionScriptSource.toSimpleName(type);
            return "(ins.readField(" + type + ") as " + type + ").value";

        } else if (type.equals("byte")) {
            return "ins.readByte()";

        } else if (type.equals("short")) {
            return "ins.readShort()";

        } else if (type.equals("int")) {
            return "ins.readInt()";

        } else if (type.equals("long")) {
            return "new Long(ins.readInt(), ins.readInt())";

        } else {
            type = ActionScriptSource.toSimpleName(type);
            return "(ins.readObject() as " + type + ")";
        }
    }

    protected String toWriteObject (String type, String name)
    {
        if (type.equals("java.lang.Integer")) {
            return "writeObject(new Integer(" + name + "))";

        } else if (type.equals("byte")) {
            return "writeByte(" + name + ")";

        } else if (type.equals("short")) {
            return "writeShort(" + name + ")";

        } else if (type.equals("int")) {
            return "writeInt(" + name + ")";

        } else if (type.equals("long")) {
            return "writeInt(" + name + " == null ? 0 : " + name + ".low);\n" +
                "        out.writeInt(" +
                name + " == null ? 0 : " + name + ".high)";

        } else if (type.equals("java.lang.String")) {
            return "writeField(" + name + ")";

        } else {
            return "writeObject(" + name + ")";
        }
    }

    /** A list of filesets that contain tile images. */
    protected ArrayList<FileSet> _filesets = new ArrayList<FileSet>();

    /** A header to put on all generated source files. */
    protected String _header;

    /** The path to our ActionScript source files. */
    protected File _asroot;

    /** Used to do our own classpath business. */
    protected ClassLoader _cloader;

    /** Used to generate source files from templates. */
    protected VelocityEngine _velocity;

    /** {@link Streamable} resolved with the proper classloader so that we can
     * compare it to loaded derived classes. */
    protected Class<?> _sclass;

    /** {@link DObject} resolved with the proper classloader so that we
     * can compare it to loaded derived classes. */
    protected Class<?> _doclass;

    /** {@link InvocationMarshaller} resolved with the proper classloader so
     * that we can compare it to loaded derived classes. */
    protected Class<?> _imclass;

    protected static final String READ_SIG =
        "public function readObject (ins :ObjectInputStream) :void";
    protected static final String WRITE_SIG =
        "public function writeObject (out :ObjectOutputStream) :void";
}
