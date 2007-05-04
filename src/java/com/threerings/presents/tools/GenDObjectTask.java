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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ClasspathUtils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.samskivert.util.ObjectUtil;
import com.samskivert.util.SortableArrayList;
import com.samskivert.util.StringUtil;
import com.samskivert.velocity.VelocityUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.OidList;

/**
 * Generates necessary additional distributed object declarations and
 * methods.
 */
public class GenDObjectTask extends Task
{
    /**
     * Adds a nested &lt;fileset&gt; element which enumerates service
     * declaration source files.
     */
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }

    /** Configures our classpath which we'll use to load service classes. */
    public void setClasspathref (Reference pathref)
    {
        _cloader = ClasspathUtils.getClassLoaderForPath(
            getProject(), pathref);
    }

    /** Performs the actual work of the task. */
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

        // resolve the DObject class using our classloader
        try {
            _doclass = _cloader.loadClass(DObject.class.getName());
            _dsclass = _cloader.loadClass(DSet.class.getName());
            _olclass = _cloader.loadClass(OidList.class.getName());
        } catch (Exception e) {
            throw new BuildException("Can't resolve InvocationListener", e);
        }

        for (FileSet fs : _filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (int f = 0; f < srcFiles.length; f++) {
                processObject(new File(fromDir, srcFiles[f]));
            }
        }
    }

    /** Processes a distributed object source file. */
    protected void processObject (File source)
    {
        // System.err.println("Processing " + source + "...");
        // load up the file and determine it's package and classname
        String name = null;
        try {
            name = GenUtil.readClassName(source);
        } catch (Exception e) {
            System.err.println(
                "Failed to parse " + source + ": " + e.getMessage());
        }

        try {
            processObject(source, _cloader.loadClass(name));
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

    /** Processes a resolved distributed object class instance. */
    protected void processObject (File source, Class oclass)
    {
        // make sure we extend distributed object
        if (!_doclass.isAssignableFrom(oclass) || _doclass.equals(oclass)) {
            // System.err.println("Skipping " + oclass.getName() + "...");
            return;
        }

        // determine which fields we need to deal with
        ArrayList<Field> flist = new ArrayList<Field>();
        Field[] fields = oclass.getDeclaredFields();
        for (int ii = 0; ii < fields.length; ii++) {
            Field f = fields[ii];
            int mods = f.getModifiers();
            if (!Modifier.isPublic(mods) ||
                Modifier.isStatic(mods) ||
                Modifier.isTransient(mods)) {
                continue;
            }
            flist.add(f);
        }

        // slurp our source file into newline separated strings
        SourceFile sfile = new SourceFile();
        try {
            sfile.readFrom(source);
        } catch (IOException ioe) {
            System.err.println("Error reading '" + source + "': " + ioe);
            return;
        }

        // generate our fields section and our methods section
        StringBuilder fsection = new StringBuilder();
        StringBuilder msection = new StringBuilder();
        for (int ii = 0; ii < flist.size(); ii++) {
            Field f = flist.get(ii);
            Class<?> ftype = f.getType();
            String fname = f.getName();

            // create our velocity context
            VelocityContext ctx = new VelocityContext();
            ctx.put("field", fname);
            ctx.put("type", GenUtil.simpleName(f));
            ctx.put("wrapfield", GenUtil.boxArgument(ftype, "value"));
            ctx.put("wrapofield", GenUtil.boxArgument(ftype, "ovalue"));
            ctx.put("clonefield", GenUtil.cloneArgument(_dsclass, f, "value"));
            ctx.put("capfield", StringUtil.unStudlyName(fname).toUpperCase());
            ctx.put("upfield", StringUtils.capitalize(fname));

            // if this field is an array, we need its component types
            if (ftype.isArray()) {
                Class<?> etype = ftype.getComponentType();
                ctx.put("elemtype", GenUtil.simpleName(etype, null));
                ctx.put("wrapelem", GenUtil.boxArgument(etype, "value"));
                ctx.put("wrapoelem", GenUtil.boxArgument(etype, "ovalue"));
            }

            // if this field is a generic DSet, we need its bound type
            if (_dsclass.isAssignableFrom(ftype)) {
                Type t = f.getGenericType();
                // we need to walk up the heirarchy until we get to the
                // parameterized DSet
                while (t instanceof Class<?>) {
                    t = ((Class<?>)t).getGenericSuperclass();
                }
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)t;
                    if (pt.getActualTypeArguments().length > 0) {
                        ctx.put("etype", GenUtil.simpleName(
                                    (Class<?>)pt.getActualTypeArguments()[0],
                                    null));
                    }
                } else {
                    ctx.put("etype", "DSet.Entry");
                }
            }

            // now figure out which template to use
            String tname = "field.tmpl";
            if (_dsclass.isAssignableFrom(ftype)) {
                tname = "set.tmpl";
            } else if (_olclass.isAssignableFrom(ftype)) {
                tname = "oidlist.tmpl";
            }

            // now generate our bits
            StringWriter fwriter = new StringWriter();
            StringWriter mwriter = new StringWriter();
            try {
                _velocity.mergeTemplate(NAME_TMPL, "UTF-8", ctx, fwriter);
                _velocity.mergeTemplate(
                    BASE_TMPL + tname, "UTF-8", ctx, mwriter);
            } catch (Exception e) {
                System.err.println("Failed processing template");
                e.printStackTrace(System.err);
            }

            // and append them as appropriate to the string buffers
            if (ii > 0) {
                fsection.append("\n");
                msection.append("\n");
            }
            fsection.append(fwriter.toString());
            msection.append(mwriter.toString());
        }

        // now bolt everything back together into a class declaration
        try {
            sfile.writeTo(source, fsection.toString(), msection.toString());
        } catch (IOException ioe) {
            System.err.println("Error writing '" + source + "': " + ioe);
        }
    }

    /** A list of filesets that contain tile images. */
    protected ArrayList<FileSet> _filesets = new ArrayList<FileSet>();

    /** Used to do our own classpath business. */
    protected ClassLoader _cloader;

    /** Used to generate source files from templates. */
    protected VelocityEngine _velocity;

    /** {@link DObject} resolved with the proper classloader so that we
     * can compare it to loaded derived classes. */
    protected Class<?> _doclass;

    /** {@link DSet} resolved with the proper classloader so that we can
     * compare it to loaded derived classes. */
    protected Class<?> _dsclass;

    /** {@link OidList} resolved with the proper classloader so that we
     * can compare it to loaded derived classes. */
    protected Class<?> _olclass;

    /** Specifies the start of the path to our various templates. */
    protected static final String BASE_TMPL =
        "com/threerings/presents/tools/dobject_";

    /** Specifies the path to the name code template. */
    protected static final String NAME_TMPL = BASE_TMPL + "name.tmpl";
}
