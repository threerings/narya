//
// $Id$

package com.threerings.presents.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ClasspathUtils;

import org.apache.velocity.app.VelocityEngine;

import com.samskivert.util.ObjectUtil;
import com.samskivert.util.SortableArrayList;
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
    public static class ListenerArgument
    {
        public int index;

        public Class listener;

        public ListenerArgument (int index, Class listener)
        {
            this.index = index+1;
            this.listener = listener;
        }

        public String getMarshaller ()
        {
            String name = simpleName(listener);
            // handle ye olde special case
            if (name.equals("InvocationService.InvocationListener")) {
                return "ListenerMarshaller";
            }
            name = StringUtil.replace(name, "Service", "Marshaller");
            return StringUtil.replace(name, "Listener", "Marshaller");
        }
    }

    /** Used to keep track of invocation service methods. */
    public static class ServiceMethod
    {
        public Method method;

        public ArrayList listenerArgs = new ArrayList();

        public ServiceMethod (Class service, Method method, HashMap imports)
        {
            this.method = method;

            // we need to look through our arguments and note any needed
            // imports in the supplied table
            Class[] args = method.getParameterTypes();
            for (int ii = 0; ii < args.length; ii++) {
                Class arg = args[ii];
                while (arg.isArray()) {
                    arg = arg.getComponentType();
                }

                // if this is a listener, we need to add a listener
                // argument info for it
                if (InvocationListener.class.isAssignableFrom(arg)) {
                    listenerArgs.add(new ListenerArgument(ii, arg));
                }

                // if it's not primitive, global or in our package, we
                // should add an import statement for it
                if (arg.isPrimitive() ||
                    arg.getName().startsWith("java.lang") ||
                    ObjectUtil.equals(
                        arg.getPackage(), service.getPackage())) {
                    continue;
                }
                imports.put(importify(arg.getName()), Boolean.TRUE);

                // if it's a listener and not one of the special
                // InvocationService listeners, we need to import its
                // marshaller as well
                if (InvocationListener.class.isAssignableFrom(arg) &&
                    !simpleName(arg).startsWith("InvocationService")) {
                    String mname = arg.getName();
                    mname = StringUtil.replace(mname, "Service", "Marshaller");
                    mname = StringUtil.replace(mname, "Listener", "Marshaller");
                    mname = StringUtil.replace(mname, ".client.", ".data.");
                    imports.put(importify(mname), Boolean.TRUE);
                }
            }
        }

        public String getCode ()
        {
            return StringUtil.unStudlyName(method.getName()).toUpperCase();
        }

        public String getArgList ()
        {
            StringBuffer buf = new StringBuffer();
            Class[] args = method.getParameterTypes();
            for (int ii = 0; ii < args.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(simpleName(args[ii])).append(" arg").append(ii+1);
            }
            return buf.toString();
        }

        public String getWrappedArgList (boolean skipFirst)
        {
            StringBuffer buf = new StringBuffer();
            Class[] args = method.getParameterTypes();
            for (int ii = (skipFirst ? 1 : 0); ii < args.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(wrapArgument(args[ii], ii+1));
            }
            return buf.toString();
        }

        public boolean hasArgs ()
        {
            return (method.getParameterTypes().length > 1);
        }

        public String getUnwrappedArgList (boolean listenerMode)
        {
            StringBuffer buf = new StringBuffer();
            Class[] args = method.getParameterTypes();
            for (int ii = (listenerMode ? 0 : 1); ii < args.length; ii++) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(unwrapArgument(args[ii], listenerMode ? ii : ii-1,
                                          listenerMode));
            }
            return buf.toString();
        }

        protected String wrapArgument (Class clazz, int index)
        {
            if (clazz == Boolean.TYPE) {
                return "new Boolean(arg" + index + ")";
            } else if (clazz == Byte.TYPE) {
                return "new Byte(arg" + index + ")";
            } else if (clazz == Character.TYPE) {
                return "new Character(arg" + index + ")";
            } else if (clazz == Short.TYPE) {
                return "new Short(arg" + index + ")";
            } else if (clazz == Integer.TYPE) {
                return "new Integer(arg" + index + ")";
            } else if (clazz == Long.TYPE) {
                return "new Long(arg" + index + ")";
            } else if (clazz == Float.TYPE) {
                return "new Float(arg" + index + ")";
            } else if (clazz == Double.TYPE) {
                return "new Double(arg" + index + ")";
            } else if (InvocationListener.class.isAssignableFrom(clazz)) {
                return "listener" + index;
            } else {
                return "arg" + index;
            }
        }

        protected String unwrapArgument (
            Class clazz, int index, boolean listenerMode)
        {
            if (clazz == Boolean.TYPE) {
                return "((Boolean)args[" + index + "]).booleanValue()";
            } else if (clazz == Byte.TYPE) {
                return "((Byte)args[" + index + "]).byteValue()";
            } else if (clazz == Character.TYPE) {
                return "((Character)args[" + index + "]).charValue()";
            } else if (clazz == Short.TYPE) {
                return "((Short)args[" + index + "]).shortValue()";
            } else if (clazz == Integer.TYPE) {
                return "((Integer)args[" + index + "]).intValue()";
            } else if (clazz == Long.TYPE) {
                return "((Long)args[" + index + "]).longValue()";
            } else if (clazz == Float.TYPE) {
                return "((Float)args[" + index + "]).floatValue()";
            } else if (clazz == Double.TYPE) {
                return "((Double)args[" + index + "]).doubleValue()";
            } else if (listenerMode &&
                       InvocationListener.class.isAssignableFrom(clazz)) {
                return "listener" + index;
            } else {
                return "(" + simpleName(clazz) + ")args[" + index + "]";
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

        ArrayList files = new ArrayList();
        for (Iterator iter = _filesets.iterator(); iter.hasNext(); ) {
            FileSet fs = (FileSet)iter.next();
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
        String pkgname = null, name = null;
        try {
            BufferedReader bin = new BufferedReader(new FileReader(source));
            String line;
            while ((line = bin.readLine()) != null) {
                Matcher pm = PACKAGE_PATTERN.matcher(line);
                if (pm.find()) {
                    pkgname = pm.group(1);
                }
                Matcher nm = NAME_PATTERN.matcher(line);
                if (nm.find()) {
                    name = nm.group(1);
                    break;
                }
            }
            bin.close();

            // make sure we found something
            if (name == null) {
                System.err.println(
                    "Unable to locate interface name in " + source + ".");
                return;
            }

            // prepend the package name to get a name we can Class.forName()
            if (pkgname != null) {
                name = pkgname + "." + name;
            }

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
        if (_header != null) {
            data = _header + data;
        }
        FileUtils.writeStringToFile(new File(path), data, "UTF-8");
    }

    protected static void checkedAdd (SortableArrayList list, String value)
    {
        if (!list.contains(value)) {
            list.add(value);
        }
    }

    protected static String simpleName (Class clazz)
    {
        if (clazz.isArray()) {
            return simpleName(clazz.getComponentType()) + "[]";
        } else {
            Package pkg = clazz.getPackage();
            int offset = (pkg == null) ? 0 : pkg.getName().length()+1;
            String name = clazz.getName().substring(offset);
            return StringUtil.replace(name, "$", ".");
        }
    }

    protected static String importify (String name)
    {
        int didx = name.indexOf("$");
        return (didx == -1) ? name : name.substring(0, didx);
    }

    /** A list of filesets that contain tile images. */
    protected ArrayList _filesets = new ArrayList();

    /** A header to put on all generated source files. */
    protected String _header;

    /** Used to do our own classpath business. */
    protected ClassLoader _cloader;

    /** Used to generate source files from templates. */
    protected VelocityEngine _velocity;

    /** A regular expression for matching the package declaration. */
    protected static final Pattern PACKAGE_PATTERN =
        Pattern.compile("^\\s*package\\s+(\\S+)\\W");

    /** A regular expression for matching the interface declaration. */
    protected static final Pattern NAME_PATTERN =
        Pattern.compile("^\\s*public\\s+interface\\s+(\\S+)(\\W|$)");
}
