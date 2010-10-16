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

import java.util.List;
import java.util.Map;

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

import com.samskivert.velocity.VelocityUtil;

public abstract class GenTask extends Task
{
    public GenTask ()
    {
        try {
            _velocity = VelocityUtil.createEngine();
        } catch (Exception e) {
            throw new BuildException("Failure initializing Velocity", e);
        }
    }

    /**
     * Adds a nested &lt;fileset&gt; element which enumerates service declaration source files.
     */
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }

    /**
     * Configures to output extra information when generating code.
     */
    public void setVerbose (boolean verbose)
    {
        _verbose = verbose;
    }

    /** Configures our classpath which we'll use to load service classes. */
    public void setClasspathref (Reference pathref)
    {
        _cloader = ClasspathUtils.getClassLoaderForPath(getProject(), pathref);

        // set the parent of the classloader to be the classloader used to load this task, rather
        // than the classloader used to load Ant, so that we have access to Narya classes like
        // TransportHint
        ((AntClassLoader)_cloader).setParent(getClass().getClassLoader());
    }

    /**
     * Performs the actual work of the task.
     */
    @Override
    public void execute ()
    {
        for (FileSet fs : _filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (String srcFile : srcFiles) {
                File source = new File(fromDir, srcFile);
                try {
                    processClass(source, loadClass(source));
                } catch (Exception e) {
                    throw new BuildException(e);
                }
            }
        }
    }

    /**
     * Merges the specified template using the supplied mapping of keys to objects.
     *
     * @param data a series of key, value pairs where the keys must be strings and the values can
     * be any object.
     */
    protected String mergeTemplate (String template, Object... data)
        throws Exception
    {
        VelocityContext ctx = new VelocityContext();
        for (int ii = 0; ii < data.length; ii += 2) {
            ctx.put((String)data[ii], data[ii+1]);
        }
        return mergeTemplate(template, ctx);
    }

    /**
     * Merges the specified template using the supplied mapping of string keys to objects.
     *
     * @return a string containing the merged text.
     */
    protected String mergeTemplate (String template, Map<String, Object> data)
        throws Exception
    {
        VelocityContext ctx = new VelocityContext();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            ctx.put(entry.getKey(), entry.getValue());
        }
        return mergeTemplate(template, ctx);
    }

    /**
     * A helper function for {@link #mergeTemplate(String, Map<String, Object>)} and friends. Don't
     * use this directly as you'll end up depending on Velocity and your code won't build.
     */
    protected String mergeTemplate (String template, VelocityContext ctx)
        throws Exception
    {
        StringWriter writer = new StringWriter();
        _velocity.mergeTemplate(template, "UTF-8", ctx, writer);
        return writer.toString();
    }

    /**
     * Process a class found from the given source file that was on the filesets given to this
     * task.
     */
    protected abstract void processClass (File source, Class<?> klass)
        throws Exception;

    protected Class<?> loadClass (File source)
    {
        // load up the file and determine it's package and classname
        String name;
        try {
            name = GenUtil.readClassName(source);
        } catch (Exception e) {
            throw new BuildException("Failed to parse " + source + ": " + e.getMessage());
        }
        return loadClass(name);
    }

    protected Class<?> loadClass (String name)
    {
        if (_cloader == null) {
            throw new BuildException("This task requires a 'classpathref' attribute " +
                "to be set to the project's classpath.");
        }
        try {
            return _cloader.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            throw new BuildException(
                "Failed to load " + name + ".  Be sure to set the 'classpathref' attribute to a " +
                "classpath that contains your project's presents classes.", cnfe);
        }
    }

    /** A list of filesets that contain java source to be processed. */
    protected List<FileSet> _filesets = Lists.newArrayList();

    /** Show extra output if set. */
    protected boolean _verbose;

    /** Used to do our own classpath business. */
    protected ClassLoader _cloader;

    /** Used to generate source files from templates. Don't use this directly from derived classes,
     * use {@link #mergeTemplate}. */
    protected VelocityEngine _velocity;
}
