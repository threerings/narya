//
// $Id$

package com.threerings.presents.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.samskivert.util.CollectionUtil;
import com.samskivert.util.ObjectUtil;
import com.samskivert.util.SortableArrayList;
import com.samskivert.util.StringUtil;
import com.samskivert.velocity.VelocityUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.InvocationListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * An Ant task for generating invocation service marshalling and
 * unmarshalling classes.
 */
public class GenServiceTask extends Task
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

    /** Used to keep track of custom InvocationListener derivations. */
    public static class ServiceListener
    {
        public Class listener;

        public ArrayList methods = new ArrayList();

        public ServiceListener (Class service, Class listener, HashMap imports)
        {
            this.listener = listener;
            Method[] methdecls = listener.getDeclaredMethods();
            for (int ii = 0; ii < methdecls.length; ii++) {
                Method m = methdecls[ii];
                // service interface methods must be public and abstract
                if (!Modifier.isPublic(m.getModifiers()) &&
                    !Modifier.isAbstract(m.getModifiers())) {
                    continue;
                }
                methods.add(new ServiceMethod(service, m, imports));
            }
        }

        public String getName ()
        {
            String name = simpleName(listener);
            name = StringUtil.replace(name, "Listener", "");
            int didx = name.indexOf(".");
            return name.substring(didx+1);
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
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
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

    /**
     * Processes an invocation service source file.
     */
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
            processService(source, Class.forName(name));
        } catch (Exception e) {
            System.err.println(
                "Failed to load " + name + ": " + e.getMessage());
            System.err.println("Make sure the classes for which you will " +
                               "be generating interfaces are");
            System.err.println("in ant's classpath.");
        }
    }

    /**
     * Processes a resolved invocation service class instance.
     */
    protected void processService (File source, Class service)
    {
        System.out.println("Processing " + service.getName() + "...");
        String sname = service.getName();
        String spackage = "";
        int didx = sname.lastIndexOf(".");
        if (didx != -1) {
            spackage = sname.substring(0, didx);
            sname = sname.substring(didx+1);
        }

        // verify that the service class name is as we expect it to be
        if (!sname.endsWith("Service")) {
            System.err.println("Cannot process '" + sname + "':");
            System.err.println(
                "Service classes must be named SomethingService.");
            return;
        }

        HashMap imports = new HashMap();
        ArrayList methods = new ArrayList();
        ArrayList listeners = new ArrayList();

        // we need to import the service itself
        imports.put(importify(service.getName()), Boolean.TRUE);

        // look through and locate our service methods, also locating any
        // custom InvocationListener derivations along the way
        Method[] methdecls = service.getDeclaredMethods();
        for (int ii = 0; ii < methdecls.length; ii++) {
            Method m = methdecls[ii];
            // service interface methods must be public and abstract
            if (!Modifier.isPublic(m.getModifiers()) &&
                !Modifier.isAbstract(m.getModifiers())) {
                continue;
            }
            // check this method for custom listener declarations
            Class[] args = m.getParameterTypes();
            for (int aa = 0; aa < args.length; aa++) {
                if (InvocationListener.class.isAssignableFrom(args[aa]) &&
                    simpleName(args[aa]).startsWith(sname + ".")) {
                    listeners.add(new ServiceListener(
                                      service, args[aa], imports));
                }
            }
            methods.add(new ServiceMethod(service, m, imports));
        }

//         String dname = StringUtil.replace(sname, "Service", "Dispatcher");
//         String dpackage = StringUtil.replace(spackage, ".client", ".server");
//         String pname = StringUtil.replace(sname, "Service", "Provider");
//         String ppackage = StringUtil.replace(spackage, ".client", ".server");

        generateMarshaller(source, sname, spackage, methods, listeners,
                           imports.keySet().iterator());
        generateDispatcher(source, sname, spackage, methods,
                           imports.keySet().iterator());
    }

    protected void generateMarshaller (
        File source, String sname, String spackage, ArrayList methods,
        ArrayList listeners, Iterator imports)
    {
        String name = StringUtil.replace(sname, "Service", "");
        String mname = StringUtil.replace(sname, "Service", "Marshaller");
        String mpackage = StringUtil.replace(spackage, ".client", ".data");

        // construct our imports list
        SortableArrayList implist = new SortableArrayList();
        CollectionUtil.addAll(implist, imports);
        checkedAdd(implist, Client.class.getName());
        checkedAdd(implist, InvocationMarshaller.class.getName());
        checkedAdd(implist, InvocationResponseEvent.class.getName());
        implist.sort();

        VelocityContext ctx = new VelocityContext();
        ctx.put("name", name);
        ctx.put("package", mpackage);
        ctx.put("methods", methods);
        ctx.put("listeners", listeners);
        ctx.put("imports", implist);

        try {
            StringWriter sw = new StringWriter();
            _velocity.mergeTemplate(MARSHALLER_TMPL, "UTF-8", ctx, sw);

            // determine the path to our marshaller file
            String mpath = source.getPath();
            mpath = StringUtil.replace(mpath, "Service", "Marshaller");
            mpath = StringUtil.replace(mpath, "/client/", "/data/");

            System.out.println("Generating " + mname + "...");
            writeFile(mpath, sw.toString());

        } catch (Exception e) {
            System.err.println("Failed processing template");
            e.printStackTrace(System.err);
        }
    }

    protected void generateDispatcher (
        File source, String sname, String spackage, ArrayList methods,
        Iterator imports)
    {
        String name = StringUtil.replace(sname, "Service", "");
        String dname = StringUtil.replace(sname, "Service", "Dispatcher");
        String dpackage = StringUtil.replace(spackage, ".client", ".server");

        // construct our imports list
        SortableArrayList implist = new SortableArrayList();
        CollectionUtil.addAll(implist, imports);
        checkedAdd(implist, ClientObject.class.getName());
        checkedAdd(implist, InvocationMarshaller.class.getName());
        checkedAdd(implist, InvocationDispatcher.class.getName());
        checkedAdd(implist, InvocationException.class.getName());
        String mname = StringUtil.replace(sname, "Service", "Marshaller");
        String mpackage = StringUtil.replace(spackage, ".client", ".data");
        checkedAdd(implist, mpackage + "." + mname);
        implist.sort();

        VelocityContext ctx = new VelocityContext();
        ctx.put("name", name);
        ctx.put("package", dpackage);
        ctx.put("methods", methods);
        ctx.put("imports", implist);

        try {
            StringWriter sw = new StringWriter();
            _velocity.mergeTemplate(DISPATCHER_TMPL, "UTF-8", ctx, sw);

            // determine the path to our marshaller file
            String mpath = source.getPath();
            mpath = StringUtil.replace(mpath, "Service", "Dispatcher");
            mpath = StringUtil.replace(mpath, "/client/", "/server/");

            System.out.println("Generating " + dname + "...");
            writeFile(mpath, sw.toString());

        } catch (Exception e) {
            System.err.println("Failed processing template");
            e.printStackTrace(System.err);
        }
    }

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

    /** Used to generate source files from templates. */
    protected VelocityEngine _velocity;

    /** A regular expression for matching the package declaration. */
    protected static final Pattern PACKAGE_PATTERN =
        Pattern.compile("^\\s*package\\s+(\\S+)\\W");

    /** A regular expression for matching the interface declaration. */
    protected static final Pattern NAME_PATTERN =
        Pattern.compile("^\\s*public\\s+interface\\s+(\\S+)\\W");

    /** Specifies the path to the marshaller template. */
    protected static final String MARSHALLER_TMPL =
        "com/threerings/presents/tools/marshaller.tmpl";

    /** Specifies the path to the dispatcher template. */
    protected static final String DISPATCHER_TMPL =
        "com/threerings/presents/tools/dispatcher.tmpl";
}
