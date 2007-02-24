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
import java.io.StringWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.VelocityContext;

import com.samskivert.util.ComparableArrayList;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
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
    public class ServiceListener implements Comparable<ServiceListener>
    {
        public Class listener;

        public ComparableArrayList<ServiceMethod> methods =
            new ComparableArrayList<ServiceMethod>();

        public ServiceListener (Class service, Class listener,
                                HashMap<String,Boolean> imports,
                                HashMap<String,Boolean> rawimports)
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
                methods.add(new ServiceMethod(service, m, imports, rawimports));
            }
            methods.sort();
        }

        public int compareTo (ServiceListener other)
        {
            return getName().compareTo(other.getName());
        }

        public boolean equals (Object other)
        {
            return getClass().equals(other.getClass()) &&
                listener.equals(((ServiceListener)other).listener);
        }

        public String getName ()
        {
            String name = GenUtil.simpleName(listener, null);
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

    /**
     * Configures the path to our ActionScript source files.
     */
    public void setAsroot (File asroot)
    {
        _asroot = asroot;
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

        HashMap<String,Boolean> imports = new HashMap<String,Boolean>();
        HashMap<String,Boolean> rawimports = new HashMap<String,Boolean>();
        ComparableArrayList<ServiceMethod> methods =
            new ComparableArrayList<ServiceMethod>();
        ComparableArrayList<ServiceListener> listeners =
            new ComparableArrayList<ServiceListener>();

        // we need to import the service itself
        imports.put(importify(service.getName()), Boolean.TRUE);
        rawimports.put(service.getName(), Boolean.TRUE);

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
                    GenUtil.simpleName(
                        args[aa], null).startsWith(sname + ".")) {
                    checkedAdd(listeners, new ServiceListener(
                                   service, args[aa], imports, rawimports));
                }
            }
            methods.add(new ServiceMethod(service, m, imports, rawimports));
        }
        listeners.sort();
        methods.sort();

        generateMarshaller(source, sname, spackage, methods, listeners,
                           imports.keySet(), rawimports.keySet());
        generateDispatcher(source, sname, spackage, methods,
                           imports.keySet());
        if (!_providerless.contains(sname)) {
            generateProvider(source, sname, spackage, methods, listeners,
                             imports.keySet());
        }
    }

    protected void generateMarshaller (
        File source, String sname, String spackage, List methods,
        List<ServiceListener> listeners, Collection<String> imports,
        Collection<String> rawimports)
    {
        String name = StringUtil.replace(sname, "Service", "");
        String mname = StringUtil.replace(sname, "Service", "Marshaller");
        String mpackage = StringUtil.replace(spackage, ".client", ".data");

        // construct our imports list
        ComparableArrayList<String> implist = new ComparableArrayList<String>();
        implist.addAll(imports);
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

        // determine the path to our marshaller file
        String mpath = source.getPath();
        mpath = StringUtil.replace(mpath, "Service", "Marshaller");
        mpath = replacePath(mpath, "/client/", "/data/");

        try {
            StringWriter sw = new StringWriter();
            _velocity.mergeTemplate(MARSHALLER_TMPL, "UTF-8", ctx, sw);
            writeFile(mpath, sw.toString());

        } catch (Exception e) {
            System.err.println("Failed processing template");
            e.printStackTrace(System.err);
        }

        // if we're not configured with an ActionScript source root, don't
        // generate the ActionScript versions
        if (_asroot == null) {
            return;
        }

        // convert the raw imports into ActionScript versions (inner-classes
        // become Foo_Bar)
        Collection<String> asimports = new ArrayList<String>();
        for (String impy : rawimports) {
            asimports.add(impy.replace("$", "_"));
        }

        // recreate our service imports using those
        implist = new ComparableArrayList<String>();
        implist.addAll(asimports);
        checkedAdd(implist, Client.class.getName());
        checkedAdd(implist, InvocationMarshaller.class.getName());
        Class imlm = InvocationMarshaller.ListenerMarshaller.class;
        checkedAdd(implist, imlm.getName().replace("$", "_"));
        implist.sort();
        ctx.put("imports", implist);

        // now generate ActionScript versions of our marshaller
        try {
            // make sure our marshaller directory exists
            String mppath = mpackage.replace('.', File.separatorChar);
            new File(_asroot + File.separator + mppath).mkdirs();

            // generate an ActionScript version of our marshaller
            String ampath = _asroot + File.separator + mppath +
                File.separator + mname + ".as";
            StringWriter sw = new StringWriter();
            _velocity.mergeTemplate(AS_MARSHALLER_TMPL, "UTF-8", ctx, sw);
            writeFile(ampath, sw.toString());

            // now generate ActionScript versions of our listener marshallers
            // because those have to be in separate files
            for (ServiceListener listener : listeners) {
                // recreate our imports with just what we need here
                implist = new ComparableArrayList<String>();
                implist.addAll(imports);
                checkedAdd(implist, imlm.getName().replace("$", "_"));
                String lname = listener.listener.getName();
                checkedAdd(implist, lname.replace("$", "_"));
                implist.sort();
                ctx.put("imports", implist);

                ctx.put("listener", listener);
                sw = new StringWriter();
                _velocity.mergeTemplate(
                    AS_LISTENER_MARSHALLER_TMPL, "UTF-8", ctx, sw);
                String aslpath = _asroot + File.separator + mppath +
                    File.separator + mname + "_" +
                    listener.getName() + "Marshaller.as";
                writeFile(aslpath, sw.toString());
            }

        } catch (Exception e) {
            System.err.println("Failed processing template");
            e.printStackTrace(System.err);
        }

        // then make some changes to the context and generate ActionScript
        // versions of the service interface itself
        implist = new ComparableArrayList<String>();
        implist.addAll(asimports);
        checkedAdd(implist, Client.class.getName());
        checkedAdd(implist, InvocationService.class.getName());
        Class isil = InvocationService.InvocationListener.class;
        checkedAdd(implist, isil.getName().replace("$", "_"));
        implist.sort();
        ctx.put("imports", implist);
        ctx.put("package", spackage);

        try {
            // make sure our service directory exists
            String sppath = spackage.replace('.', File.separatorChar);
            new File(_asroot + File.separator + sppath).mkdirs();

            // generate an ActionScript version of our service
            String aspath = _asroot + File.separator + sppath +
                File.separator + sname + ".as";
            StringWriter sw = new StringWriter();
            _velocity.mergeTemplate(AS_SERVICE_TMPL, "UTF-8", ctx, sw);
            writeFile(aspath, sw.toString());

            // also generate ActionScript versions of any inner listener
            // interfaces because those have to be in separate files
            for (ServiceListener listener : listeners) {
                // recreate our imports with just what we need here
                implist = new ComparableArrayList<String>();
                implist.addAll(asimports);
                checkedAdd(implist, isil.getName().replace("$", "_"));
                String lname = listener.listener.getName();
                checkedAdd(implist, lname.replace("$", "_"));
                implist.sort();
                ctx.put("imports", implist);

                ctx.put("listener", listener);
                sw = new StringWriter();
                _velocity.mergeTemplate(
                    AS_LISTENER_SERVICE_TMPL, "UTF-8", ctx, sw);
                String amlpath = _asroot + File.separator + sppath +
                    File.separator + sname + "_" +
                    listener.getName() + "Listener.as";
                writeFile(amlpath, sw.toString());
            }

        } catch (Exception e) {
            System.err.println("Failed processing template");
            e.printStackTrace(System.err);
        }
    }

    protected void generateDispatcher (
        File source, String sname, String spackage, List methods,
        Collection<String> imports)
    {
        String name = StringUtil.replace(sname, "Service", "");
        String dname = StringUtil.replace(sname, "Service", "Dispatcher");
        String dpackage = StringUtil.replace(spackage, ".client", ".server");

        // construct our imports list
        ComparableArrayList<String> implist = new ComparableArrayList<String>();
        implist.addAll(imports);
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
        List listeners, Collection<String> imports)
    {
        String name = StringUtil.replace(sname, "Service", "");
        String mname = StringUtil.replace(sname, "Service", "Provider");
        String mpackage = StringUtil.replace(spackage, ".client", ".server");

        // construct our imports list
        ComparableArrayList<String> implist = new ComparableArrayList<String>();
        implist.addAll(imports);
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

    /** The path to our ActionScript source files. */
    protected File _asroot;

    /** Services for which we should not generate provider interfaces. */
    protected HashSet<String> _providerless = new HashSet<String>();

    /** Specifies the path to the marshaller template. */
    protected static final String MARSHALLER_TMPL =
        "com/threerings/presents/tools/marshaller.tmpl";

    /** Specifies the path to the dispatcher template. */
    protected static final String DISPATCHER_TMPL =
        "com/threerings/presents/tools/dispatcher.tmpl";

    /** Specifies the path to the provider template. */
    protected static final String PROVIDER_TMPL =
        "com/threerings/presents/tools/provider.tmpl";

    /** Specifies the path to the ActionScript service template. */
    protected static final String AS_SERVICE_TMPL =
        "com/threerings/presents/tools/service_as.tmpl";

    /** Specifies the path to the ActionScript listener service template. */
    protected static final String AS_LISTENER_SERVICE_TMPL =
        "com/threerings/presents/tools/service_listener_as.tmpl";

    /** Specifies the path to the ActionScript marshaller template. */
    protected static final String AS_MARSHALLER_TMPL =
        "com/threerings/presents/tools/marshaller_as.tmpl";

    /** Specifies the path to the ActionScript listener marshaller template. */
    protected static final String AS_LISTENER_MARSHALLER_TMPL =
        "com/threerings/presents/tools/marshaller_listener_as.tmpl";
}
