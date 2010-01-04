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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.ArrayList;

import java.io.File;
import java.io.StringWriter;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ClasspathUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.google.common.collect.Lists;

import com.samskivert.util.StringUtil;

import com.samskivert.velocity.VelocityUtil;

import com.threerings.presents.annotation.TransportHint;
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

        // set the parent of the classloader to be the classloader used to load this task,
        // rather than the classloader used to load Ant, so that we have access to Narya
        // classes like TransportHint
        ((AntClassLoader)_cloader).setParent(getClass().getClassLoader());
    }

    @Override
    public void execute ()
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
            for (String srcFile : srcFiles) {
                processObject(new File(fromDir, srcFile));
            }
        }
    }

    /** Processes a distributed object source file. */
    protected void processObject (File source)
    {
        // load up the file and determine it's package and classname
        String name = null;
        try {
            // System.err.println("Processing " + source + "...");
            name = GenUtil.readClassName(source);
            processObject(source, _cloader.loadClass(name));
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Failed to load " + name + ".\n" +
                               "Missing class: " + cnfe.getMessage());
            System.err.println("Be sure to set the 'classpathref' attribute to a classpath\n" +
                               "that contains your projects invocation service classes.");
        } catch (Exception e) {
            throw new BuildException("Failed to process " + source.getName() + ": " + e, e);
        }
    }

    /** Processes a resolved distributed object class instance. */
    protected void processObject (File source, Class<?> oclass)
        throws Exception
    {
        // make sure we extend distributed object
        if (!_doclass.isAssignableFrom(oclass) || _doclass.equals(oclass)) {
            // System.err.println("Skipping " + oclass.getName() + "...");
            return;
        }

        // determine which fields we need to deal with
        ArrayList<Field> flist = Lists.newArrayList();
        Field[] fields = oclass.getDeclaredFields();
        for (Field f : fields) {
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
        sfile.readFrom(source);

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
            ctx.put("upfield", StringUtil.capitalize(fname));

            // determine the type of transport
            TransportHint hint = f.getAnnotation(TransportHint.class);
            if (hint == null) {
                // inherit hint from class annotation
                hint = f.getDeclaringClass().getAnnotation(TransportHint.class);
            }
            String transport;
            if (hint == null) {
                transport = "";
            } else {
                transport = ",\n" +
                    "            com.threerings.presents.net.Transport.getInstance(\n" +
                    "                com.threerings.presents.net.Transport.Type." +
                        hint.type().name() + ", " + hint.channel() + ")";
            }
            ctx.put("transport", transport);

            // if this field is an array, we need its component types
            if (ftype.isArray()) {
                Class<?> etype = ftype.getComponentType();
                ctx.put("elemtype", GenUtil.simpleName(etype));
                ctx.put("wrapelem", GenUtil.boxArgument(etype, "value"));
                ctx.put("wrapoelem", GenUtil.boxArgument(etype, "ovalue"));
            }

            // if this field is a generic DSet, we need its bound type
            if (_dsclass.isAssignableFrom(ftype)) {
                Type t = f.getGenericType();
                // we need to walk up the heirarchy until we get to the parameterized DSet
                while (t instanceof Class<?>) {
                    t = ((Class<?>)t).getGenericSuperclass();
                }
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)t;
                    if (pt.getActualTypeArguments().length > 0) {
                        ctx.put("etype", GenUtil.simpleName(pt.getActualTypeArguments()[0]));
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
            _velocity.mergeTemplate(NAME_TMPL, "UTF-8", ctx, fwriter);
            _velocity.mergeTemplate(BASE_TMPL + tname, "UTF-8", ctx, mwriter);

            // and append them as appropriate to the string buffers
            if (ii > 0) {
                fsection.append("\n");
                msection.append("\n");
            }
            fsection.append(fwriter.toString());
            msection.append(mwriter.toString());
        }

        // now bolt everything back together into a class declaration
        sfile.writeTo(source, fsection.toString(), msection.toString());
    }

    /** A list of filesets that contain tile images. */
    protected ArrayList<FileSet> _filesets = Lists.newArrayList();

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
