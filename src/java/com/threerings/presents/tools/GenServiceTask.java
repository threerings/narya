//
// $Id$

package com.threerings.presents.tools;

import java.io.File;
import java.io.StringWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.VelocityContext;

import com.samskivert.util.CollectionUtil;
import com.samskivert.util.ComparableArrayList;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.InvocationListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * An Ant task for generating invocation service marshalling and
 * unmarshalling classes.
 */
public class GenServiceTask extends InvocationTask
{
    /** Used to keep track of custom InvocationListener derivations. */
    public class ServiceListener implements Comparable
    {
        public Class listener;

        public ComparableArrayList methods = new ComparableArrayList();

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
            methods.sort();
        }

        public int compareTo (Object other)
        {
            return getName().compareTo(((ServiceListener)other).getName());
        }

        public boolean equals (Object other)
        {
            return getClass().equals(other.getClass()) &&
                listener.equals(((ServiceListener)other).listener);
        }

        public String getName ()
        {
            String name = GenUtil.simpleName(listener);
            name = StringUtil.replace(name, "Listener", "");
            int didx = name.indexOf(".");
            return name.substring(didx+1);
        }
    }

    /** Used to track services for which we should not generate a provider
     * interface. */
    public class Providerless
    {
        public void setService (String className)
        {
            _providerless.add(className);
        }
    }

    // documentation inherited
    public Providerless createProviderless ()
    {
        return new Providerless();
    }

    // documentation inherited
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
        ComparableArrayList methods = new ComparableArrayList();
        ComparableArrayList listeners = new ComparableArrayList();

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
                if (_ilistener.isAssignableFrom(args[aa]) &&
                    GenUtil.simpleName(args[aa]).startsWith(sname + ".")) {
                    checkedAdd(listeners, new ServiceListener(
                                   service, args[aa], imports));
                }
            }
            methods.add(new ServiceMethod(service, m, imports));
        }
        listeners.sort();
        methods.sort();

        generateMarshaller(source, sname, spackage, methods, listeners,
                           imports.keySet().iterator());
        generateDispatcher(source, sname, spackage, methods,
                           imports.keySet().iterator());
        if (!_providerless.contains(sname)) {
            generateProvider(source, sname, spackage, methods, listeners,
                             imports.keySet().iterator());
        }
    }

    protected void generateMarshaller (
        File source, String sname, String spackage, List methods,
        List listeners, Iterator imports)
    {
        String name = StringUtil.replace(sname, "Service", "");
        String mname = StringUtil.replace(sname, "Service", "Marshaller");
        String mpackage = StringUtil.replace(spackage, ".client", ".data");

        // construct our imports list
        ComparableArrayList implist = new ComparableArrayList();
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
            mpath = replacePath(mpath, "/client/", "/data/");

            writeFile(mpath, sw.toString());

        } catch (Exception e) {
            System.err.println("Failed processing template");
            e.printStackTrace(System.err);
        }
    }

    protected void generateDispatcher (
        File source, String sname, String spackage, List methods,
        Iterator imports)
    {
        String name = StringUtil.replace(sname, "Service", "");
        String dname = StringUtil.replace(sname, "Service", "Dispatcher");
        String dpackage = StringUtil.replace(spackage, ".client", ".server");

        // construct our imports list
        ComparableArrayList implist = new ComparableArrayList();
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
            mpath = replacePath(mpath, "/client/", "/server/");

            writeFile(mpath, sw.toString());

        } catch (Exception e) {
            System.err.println("Failed processing template");
            e.printStackTrace(System.err);
        }
    }

    protected void generateProvider (
        File source, String sname, String spackage, List methods,
        List listeners, Iterator imports)
    {
        String name = StringUtil.replace(sname, "Service", "");
        String mname = StringUtil.replace(sname, "Service", "Provider");
        String mpackage = StringUtil.replace(spackage, ".client", ".server");

        // construct our imports list
        ComparableArrayList implist = new ComparableArrayList();
        CollectionUtil.addAll(implist, imports);
        checkedAdd(implist, ClientObject.class.getName());
        checkedAdd(implist, InvocationProvider.class.getName());
        checkedAdd(implist, InvocationException.class.getName());
        implist.sort();

        VelocityContext ctx = new VelocityContext();
        ctx.put("name", name);
        ctx.put("package", mpackage);
        ctx.put("methods", methods);
        ctx.put("listeners", listeners);
        ctx.put("imports", implist);

        try {
            StringWriter sw = new StringWriter();
            _velocity.mergeTemplate(PROVIDER_TMPL, "UTF-8", ctx, sw);

            // determine the path to our provider file
            String mpath = source.getPath();
            mpath = StringUtil.replace(mpath, "Service", "Provider");
            mpath = replacePath(mpath, "/client/", "/server/");

            writeFile(mpath, sw.toString());

        } catch (Exception e) {
            System.err.println("Failed processing template");
            e.printStackTrace(System.err);
        }
    }

    /** Services for which we should not generate provider interfaces. */
    protected HashSet _providerless = new HashSet();

    /** Specifies the path to the marshaller template. */
    protected static final String MARSHALLER_TMPL =
        "com/threerings/presents/tools/marshaller.tmpl";

    /** Specifies the path to the dispatcher template. */
    protected static final String DISPATCHER_TMPL =
        "com/threerings/presents/tools/dispatcher.tmpl";

    /** Specifies the path to the provider template. */
    protected static final String PROVIDER_TMPL =
        "com/threerings/presents/tools/provider.tmpl";
}
