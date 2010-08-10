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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import java.util.Arrays;
import java.util.List;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.samskivert.io.StreamUtil;
import com.samskivert.util.Logger;
import com.samskivert.util.StringUtil;
import com.threerings.presents.annotation.TransportHint;
import com.threerings.presents.net.Transport;
import com.threerings.presents.client.InvocationService.InvocationListener;

/**
 * A base Ant task for generating invocation service related marshalling
 * and unmarshalling classes.
 */
public abstract class InvocationTask extends GenTask
{
    /** Used to keep track of invocation service method listener arguments. */
    public class ListenerArgument
    {
        public int index;

        public Class<?> listener;

        public ListenerArgument (int index, Class<?> listener)
        {
            this.index = index+1;
            this.listener = listener;
        }

        public String getMarshaller ()
        {
            String name = GenUtil.simpleName(listener);
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
                return getMarshaller().replace('.', '_');
            }
        }
    }

    /** Used to keep track of invocation service methods or listener methods. */
    public class ServiceMethod implements Comparable<ServiceMethod>
    {
        public Method method;

        public List<ListenerArgument> listenerArgs = Lists.newArrayList();

        /**
         * Creates a new service method.
         * @param method the method to inspect
         * @param imports will be filled with the types required by the method
         */
        public ServiceMethod (Method method, ImportSet imports)
        {
            this.method = method;

            // if this method has listener arguments, we need to add listener argument info for them
            Class<?>[] args = method.getParameterTypes();
            for (int ii = 0; ii < args.length; ii++) {
                Class<?> arg = args[ii];
                while (arg.isArray()) {
                    arg = arg.getComponentType();
                }

                if (_ilistener.isAssignableFrom(arg)) {
                    listenerArgs.add(new ListenerArgument(ii, arg));
                }
            }

            // we need to look through our arguments and note any needed imports in the supplied
            // table
            for (Type type : method.getGenericParameterTypes()) {
                addImportsForType(type, imports);
            }

            // import Transport if used
            if (!StringUtil.isBlank(getTransport())) {
                imports.add(Transport.class);
            }
        }

        protected void addImportsForType (Type type, ImportSet imports)
        {
            if (type instanceof Class<?>) {
                imports.add((Class<?>)type);
            } else if (type instanceof ParameterizedType) {
                imports.add((Class<?>)((ParameterizedType)type).getRawType());
                for (Type param : ((ParameterizedType)type).getActualTypeArguments()) {
                    addImportsForType(param, imports);
                }
            } else if (type instanceof WildcardType) {
                for (Type upper : ((WildcardType)type).getUpperBounds()) {
                    addImportsForType(upper, imports);
                }
                for (Type lower : ((WildcardType)type).getLowerBounds()) {
                    addImportsForType(lower, imports);
                }
            } else if (type instanceof GenericArrayType) {
                addImportsForType(((GenericArrayType)type).getGenericComponentType(), imports);
            } else {
                System.err.println(Logger.format(
                    "Unhandled Type in adding imports for a service", "type", type, "typeClass",
                    type.getClass()));
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
            Type[] ptypes = method.getGenericParameterTypes();
            for (int ii = skipFirst ? 1 : 0; ii < ptypes.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                String simpleName = GenUtil.simpleName(ptypes[ii]);
                if (method.isVarArgs() && ii == ptypes.length - 1) {
                    // Switch [] with ... for varargs
                    buf.append(simpleName.substring(0, simpleName.length() - 2)).append("...");
                } else {
                    buf.append(simpleName);
                }
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
                String index = String.valueOf(skipFirst ? ii : (ii+1));
                String arg;
                if (_ilistener.isAssignableFrom(args[ii])) {
                    arg = GenUtil.boxASArgument(args[ii],  "listener" + index);
                } else {
                    arg = GenUtil.boxASArgument(args[ii],  "arg" + index);
                }
                buf.append(arg);
            }
            return buf.toString();
        }

        public boolean hasArgs (boolean skipFirst)
        {
            return (method.getParameterTypes().length > (skipFirst ? 1 : 0));
        }

        public boolean hasParameterizedArgs ()
        {
            return Iterables.any(
                Arrays.asList(method.getGenericParameterTypes()), new Predicate<Type>() {
                public boolean apply (Type type) {
                    // TODO: might eventually need to handle generic arrays and wildcard types
                    return (type instanceof ParameterizedType);
                }
            });
        }

        public String getUnwrappedArgList (boolean listenerMode)
        {
            StringBuilder buf = new StringBuilder();
            Type[] ptypes = method.getGenericParameterTypes();
            for (int ii = (listenerMode ? 0 : 1); ii < ptypes.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(unboxArgument(ptypes[ii], listenerMode ? ii : ii-1, listenerMode));
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
                    arg = GenUtil.unboxASArgument(args[ii], "args[" + argidx + "]");
                }
                buf.append(arg);
            }
            return buf.toString();
        }

        public String getTransport ()
        {
            TransportHint hint = method.getAnnotation(TransportHint.class);
            if (hint == null) {
                // inherit hint from interface annotation
                hint = method.getDeclaringClass().getAnnotation(TransportHint.class);
            }
            if (hint == null) {
                return "";
            }
            return ", Transport.getInstance(Transport.Type." +
                hint.type().name() + ", " + hint.channel() + ")";
        }

        // from interface Comparator<ServiceMethod>
        public int compareTo (ServiceMethod other)
        {
            return getCode().compareTo(other.getCode());
        }

        @Override // from Object
        public boolean equals (Object other)
        {
            return (other instanceof ServiceMethod) && compareTo((ServiceMethod)other) == 0;
        }

        @Override // from Object
        public int hashCode ()
        {
            return getCode().hashCode();
        }

        protected String boxArgument (Class<?> clazz, int index)
        {
            if (_ilistener.isAssignableFrom(clazz)) {
                return GenUtil.boxArgument(clazz,  "listener" + index);
            } else {
                return GenUtil.boxArgument(clazz,  "arg" + index);
            }
        }

        protected String unboxArgument (Type type, int index, boolean listenerMode)
        {
            if (listenerMode && (type instanceof Class<?>) &&
                _ilistener.isAssignableFrom((Class<?>)type)) {
                return "listener" + index;
            } else {
                return GenUtil.unboxArgument(type, "args[" + index + "]");
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
            _header = StreamUtil.toString(new FileReader(header));
        } catch (IOException ioe) {
            System.err.println("Unabled to load header '" + header + ": " +
                               ioe.getMessage());
        }
    }

    @Override
    public void execute ()
    {
        // resolve the InvocationListener class using our classloader
        _ilistener = loadClass(InvocationListener.class.getName());

        for (FileSet fs : _filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (String srcFile : srcFiles) {
                processService(new File(fromDir, srcFile));
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
            throw new BuildException("Failed to parse " + source + ": " + e.getMessage());
        }

        Class<?> serviceClass = loadClass(name);
        try {
            processService(source, serviceClass);
        } catch (Exception e) {
            throw new BuildException("Failed to process " + source.getName() + ": " + e, e);
        }
    }

    /** Processes a resolved invocation service class instance. */
    protected abstract void processService (File source, Class<?> service) throws Exception;

    protected void writeFile (String path, String data)
        throws IOException
    {
        if (_verbose) {
            System.out.println("Writing file " + path);
        }
        if (_header != null) {
            data = _header + data;
        }
        new PrintWriter(new File(path), "UTF-8").append(data).close();
    }

    protected static <T> void checkedAdd (List<T> list, T value)
    {
        if (!list.contains(value)) {
            list.add(value);
        }
    }

    protected static String replacePath (String source, String oldstr, String newstr)
    {
        return source.replace(oldstr.replace('/', File.separatorChar),
                              newstr.replace('/', File.separatorChar));
    }

    /** A list of filesets that contain tile images. */
    protected List<FileSet> _filesets = Lists.newArrayList();

    /** A header to put on all generated source files. */
    protected String _header;

    /** {@link InvocationListener} resolved with the proper classloader so
     * that we can compare it to loaded derived classes. */
    protected Class<?> _ilistener;
}
