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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ClasspathUtils;

import org.apache.velocity.app.VelocityEngine;

import com.samskivert.util.StringUtil;
import com.samskivert.velocity.VelocityUtil;

import com.threerings.presents.client.InvocationService.InvocationListener;

/**
 * A base Ant task for generating invocation service related marshalling
 * and unmarshalling classes.
 */
public abstract class InvocationTask extends Task
{
    /** Used to keep track of invocation service method listener arguments. */
    public class ListenerArgument
    {
        public int index;

        public Class listener;

        public ListenerArgument (int index, Class<?> listener)
        {
            this.index = index+1;
            this.listener = listener;
        }

        public String getMarshaller ()
        {
            String name = GenUtil.simpleName(listener, null);
            // handle ye olde special case
            if (name.equals("InvocationService.InvocationListener")) {
                return "ListenerMarshaller";
            }
            name = name.replace("Service", "Marshaller");
            return name.replace("Listener", "Marshaller");
        }

        public String getActionScriptMarshaller ()
        {
            // handle ye olde special case
            String name = listener.getName();
            if (name.endsWith("InvocationService$InvocationListener")) {
                return "InvocationMarshaller_ListenerMarshaller";
            } else {
                return getMarshaller().replace(".", "_");
            }
        }
    }

    /** Used to keep track of invocation service methods or listener 
     *  methods. */
    public class ServiceMethod implements Comparable<ServiceMethod>
    {
        public Method method;

        public ArrayList<ListenerArgument> listenerArgs =
            new ArrayList<ListenerArgument>();

        /**
         * Creates a new service method.
         * @param method the method to inspect 
         * @param imports will be filled with the types required by the method
         */
        public ServiceMethod (Method method, ImportSet imports)
        {
            this.method = method;
            
            // we need to look through our arguments and note any needed
            // imports in the supplied table
            Class<?>[] args = method.getParameterTypes();
            for (int ii = 0; ii < args.length; ii++) {
                Class<?> arg = args[ii];
                while (arg.isArray()) {
                    arg = arg.getComponentType();
                }

                // if this is a listener, we need to add a listener
                // argument info for it
                if (_ilistener.isAssignableFrom(arg)) {
                    listenerArgs.add(new ListenerArgument(ii, arg));
                }

                imports.add(args[ii]);
            }
        }

        public String getCode ()
        {
            return StringUtil.unStudlyName(method.getName()).toUpperCase();
        }

        public String getSenderMethodName ()
        {
            String mname = method.getName();
            if (mname.startsWith("received")) {
                return "send" + mname.substring("received".length());
            } else {
                return mname;
            }
        }

        public String getArgList (boolean skipFirst)
        {
            StringBuilder buf = new StringBuilder();
            Class<?>[] args = method.getParameterTypes();
            Type[] ptypes = method.getGenericParameterTypes();
            for (int ii = skipFirst ? 1 : 0; ii < args.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(GenUtil.simpleName(args[ii], ptypes[ii]));
                buf.append(" arg").append(skipFirst ? ii : ii+1);
            }
            return buf.toString();
        }

        public String getASArgList (boolean skipFirst)
        {
            StringBuilder buf = new StringBuilder();
            Class<?>[] args = method.getParameterTypes();
            for (int ii = skipFirst ? 1 : 0; ii < args.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append("arg").append(skipFirst ? ii : ii+1).append(" :");
                buf.append(GenUtil.simpleASName(args[ii]));
            }
            return buf.toString();
        }

        public String getWrappedArgList (boolean skipFirst)
        {
            StringBuilder buf = new StringBuilder();
            Class<?>[] args = method.getParameterTypes();
            for (int ii = (skipFirst ? 1 : 0); ii < args.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(boxArgument(args[ii], ii+1));
            }
            return buf.toString();
        }

        public String getASWrappedArgList (boolean skipFirst)
        {
            StringBuilder buf = new StringBuilder();
            Class<?>[] args = method.getParameterTypes();
            for (int ii = (skipFirst ? 1 : 0); ii < args.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                String arg;
                if (_ilistener.isAssignableFrom(args[ii])) {
                    arg = GenUtil.boxASArgument(args[ii],  "listener" + (ii+1));
                } else {
                    arg = GenUtil.boxASArgument(args[ii],  "arg" + (ii+1));
                }
                buf.append(arg);
            }
            return buf.toString();
        }

        public boolean hasArgs (boolean skipFirst)
        {
            return (method.getParameterTypes().length > (skipFirst ? 1 : 0));
        }

        public int compareTo (ServiceMethod other)
        {
            return getCode().compareTo(other.getCode());
        }

        public String getUnwrappedArgList (boolean listenerMode)
        {
            StringBuilder buf = new StringBuilder();
            Class<?>[] args = method.getParameterTypes();
            Type[] ptypes = method.getGenericParameterTypes();
            for (int ii = (listenerMode ? 0 : 1); ii < args.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(unboxArgument(args[ii], ptypes[ii],
                               listenerMode ? ii : ii-1, listenerMode));
            }
            return buf.toString();
        }

        public String getASUnwrappedArgList (boolean listenerMode)
        {
            StringBuilder buf = new StringBuilder();
            Class<?>[] args = method.getParameterTypes();
            for (int ii = (listenerMode ? 0 : 1); ii < args.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                String arg;
                int argidx = listenerMode ? ii : ii-1;
                if (listenerMode && _ilistener.isAssignableFrom(args[ii])) {
                    arg = "listener" + argidx;
                } else {
                    arg = GenUtil.unboxASArgument(
                        args[ii], "args[" + argidx + "]");
                }
                buf.append(arg);
            }
            return buf.toString();
        }

        protected String boxArgument (Class<?> clazz, int index)
        {
            if (_ilistener.isAssignableFrom(clazz)) {
                return GenUtil.boxArgument(clazz,  "listener" + index);
            } else {
                return GenUtil.boxArgument(clazz,  "arg" + index);
            }
        }

        protected String unboxArgument (
            Class<?> clazz, Type type, int index, boolean listenerMode)
        {
            if (listenerMode && _ilistener.isAssignableFrom(clazz)) {
                return "listener" + index;
            } else {
                return GenUtil.unboxArgument(
                    clazz, type, "args[" + index + "]");
            }
        }
    }

    /**
     * Adds a nested &lt;fileset&gt; element which enumerates service
     * declaration source files.
     */
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
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
     * Configures to output extra information when generating code.
     */
    public void setVerbose (boolean verbose)
    {
        _verbose = verbose;
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

        // resolve the InvocationListener class using our classloader
        try {
            _ilistener = _cloader.loadClass(InvocationListener.class.getName());
        } catch (Exception e) {
            throw new BuildException("Can't resolve InvocationListener", e);
        }

        for (FileSet fs : _filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (int f = 0; f < srcFiles.length; f++) {
                processService(new File(fromDir, srcFiles[f]));
            }
        }
    }

    /** Processes an invocation service source file. */
    protected void processService (File source)
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
            processService(source, _cloader.loadClass(name));
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

    /** Processes a resolved invocation service class instance. */
    protected abstract void processService (File source, Class service);

    protected void writeFile (String path, String data)
        throws IOException
    {
        if (_verbose) {
            System.out.println("Writing file " + path);
        }
        if (_header != null) {
            data = _header + data;
        }
        FileUtils.writeStringToFile(new File(path), data, "UTF-8");
    }

    protected static <T> void checkedAdd (List<T> list, T value)
    {
        if (!list.contains(value)) {
            list.add(value);
        }
    }

    protected static String replacePath (
        String source, String oldstr, String newstr)
    {
        return StringUtil.replace(source,
                                  oldstr.replace('/', File.separatorChar),
                                  newstr.replace('/', File.separatorChar));
    }

    /** A list of filesets that contain tile images. */
    protected ArrayList<FileSet> _filesets = new ArrayList<FileSet>();

    /** A header to put on all generated source files. */
    protected String _header;

    /** Show extra output if set. */
    protected boolean _verbose;
    
    /** Used to do our own classpath business. */
    protected ClassLoader _cloader;

    /** Used to generate source files from templates. */
    protected VelocityEngine _velocity;

    /** {@link InvocationListener} resolved with the proper classloader so
     * that we can compare it to loaded derived classes. */
    protected Class<?> _ilistener;
}
