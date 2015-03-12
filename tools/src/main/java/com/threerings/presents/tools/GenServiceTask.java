//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.samskivert.util.StringUtil;

import com.threerings.util.ActionScript;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * An Ant task for generating invocation service marshalling and unmarshalling classes.
 *
 * <p>TODO: when generating the imports for exported action script files, there are just enough
 * conversions of primitive types (e.g. {@code float -> Number}), array types (e.g. {@code int[] ->
 * TypedArray}) and three rings utility types (e.g. {@code float -> Float}) to make the existing
 * serivces work. It should be possible to create a complete list of these conversions so that
 * future services can be generated without problems.
 */
public class GenServiceTask extends InvocationTask
{
    /** Used to keep track of custom InvocationListener derivations. */
    public class ServiceListener implements Comparable<ServiceListener>
    {
        public Class<?> listener;

        public List<ServiceMethod> methods = Lists.newArrayList();

        /** Contains all imports required for the parameters of the methods in this listener. */
        public ImportSet imports = new ImportSet();

        public ServiceListener (Class<?> service, Class<?> listener)
        {
            this.listener = listener;

            // compute the union of all InvocationListener extensions implemented by this interface
            Set<Class<?>> ifaces = Sets.newHashSet();
            addInterfaces(listener, ifaces);

            // add method marshallers for all methods in all interfaces (the marshaller will not
            // extend the marshallers for its parent interfaces and will use its own codes)
            for (Class<?> iface : ifaces) {
                Method[] methdecls = iface.getDeclaredMethods();
                for (Method m : methdecls) {
                    // service interface methods must be public and abstract
                    if (!Modifier.isPublic(m.getModifiers()) &&
                        !Modifier.isAbstract(m.getModifiers())) {
                        continue;
                    }
                    if (_verbose) {
                        System.out.println("Adding " + m + ", imports are " +
                                           StringUtil.toString(imports));
                    }
                    methods.add(createAndGatherImports(m, imports));
                    if (_verbose) {
                        System.out.println("Added " + m + ", imports are " +
                                           StringUtil.toString(imports));
                    }
                }
            }
            Collections.sort(methods);
        }

        protected void addInterfaces (Class<?> listener, Set<Class<?>> ifaces)
        {
            if (!_ilistener.isAssignableFrom(listener) || _ilistener.equals(listener)) {
                return;
            }
            ifaces.add(listener);
            for (Class<?> iface : listener.getInterfaces()) {
                addInterfaces(iface, ifaces);
            }
        }

        /**
         * Checks whether any of our methods have parameterized types.
         */
        public boolean hasParameterizedMethodArgs ()
        {
            return Iterables.any(methods, new Predicate<ServiceMethod>() {
                public boolean apply (ServiceMethod sm) {
                    return sm.hasParameterizedArgs();
                }
            });
        }

        public String getListenerName ()
        {
            String name = GenUtil.simpleName(listener);
            name = name.replace("Listener", "");
            int didx = name.indexOf(".");
            return name.substring(didx+1);
        }

        public String adapterCtorArgs () {
            StringBuilder sb = new StringBuilder();
            for (ServiceMethod m : methods) {
                sb.append(m.method.getName() + " :Function, ");
            }
            return sb.toString();
        }

        // from interface Comparable<ServiceListener>
        public int compareTo (ServiceListener other)
        {
            return getListenerName().compareTo(other.getListenerName());
        }

        @Override
        public boolean equals (Object other)
        {
            return (other != null) && getClass().equals(other.getClass()) &&
                listener.equals(((ServiceListener)other).listener);
        }

        @Override
        public int hashCode ()
        {
            return listener.getName().hashCode();
        }
    }

    /** Used to track services for which we should not generate a provider interface. */
    public class Providerless
    {
        public void setService (String className)
        {
            _providerless.add(className);
        }
    }

    /** Used to track services for which we should create listener adapters in actionscript. */
    public class Adapter
    {
        public void setService (String className)
        {
            _aslistenerAdapters.add(className);
        }
    }

    /**
     * Configures to output extra information when generating code.
     */
    public void setVerbose (boolean verbose)
    {
        _verbose = verbose;
    }

    /**
     * Configures the path to our ActionScript source files.
     */
    public void setAsroot (File asroot)
    {
        _asroot = asroot;
    }

    public Providerless createProviderless ()
    {
        return new Providerless();
    }

    public Adapter createAdapter ()
    {
        return new Adapter();
    }

    // documentation inherited
    @Override
    public void processClass (File source, Class<?> service)
        throws Exception
    {
        System.out.println("Processing " + service.getName() + "...");

        // verify that the service class name is as we expect it to be
        if (!service.getName().endsWith("Service")) {
            System.err.println("Cannot process '" + service.getName() + "':");
            System.err.println("Service classes must be named SomethingService.");
            return;
        }

        ServiceDescription desc = new ServiceDescription(service);
        generateMarshaller(source, desc);
        // generateDispatcher(source, desc); // dispatchers are no longer needed
        if (!_providerless.contains(service.getSimpleName())) {
            generateProvider(source, desc);
        }
    }

    protected void generateMarshaller (File source, ServiceDescription sdesc)
        throws Exception
    {
        if (_verbose) {
            System.out.println("Generating marshaller");
        }

        String sname = sdesc.sname;
        String name = sname.replace("Service", "");
        String mname = sname.replace("Service", "Marshaller");
        String mpackage = sdesc.spackage.replace(".client", ".data");

        // ----------- Part I - java marshaller

        // start with all imports (service methods and listener methods)
        ImportSet imports = sdesc.constructAllImports();

        // import things marshaller will always need
        imports.add(sdesc.service);
        imports.add(InvocationMarshaller.class);
        imports.add("javax.annotation.Generated");

        // We only add a type parameter for the caller ClientObject type if the service has one
        if (sdesc.callerTypeSpecified) {
            imports.add(sdesc.callerType);
        }

        // import classes contained in arrays
        imports.translateClassArrays();

        // get rid of java.lang stuff and primitives
        imports.removeGlobals();

        // get rid of all arrays (they are automatic in java)
        imports.removeArrays();

        // for each listener type, also import the corresponding marshaller
        imports.duplicateAndMunge("*Listener",
            "Service", "Marshaller",
            "Listener", "Marshaller",
            ".client.", ".data.");

        // import the parent class of Foo$Bar
        imports.swapInnerClassesForParents();

        // remove imports in our own package
        imports.removeSamePackage(mpackage);

        Map<String, Object> ctx = new HashMap<String, Object>();
        ctx.put("name", name);
        ctx.put("generated", getGeneratedAnnotation(name));
        ctx.put("package", mpackage);
        ctx.put("methods", sdesc.methods);
        ctx.put("listeners", sdesc.listeners);
        ctx.put("typeParameters",
            sdesc.callerTypeSpecified ? "<" + sdesc.callerType.getSimpleName() + ">" : "");
        ctx.put("importGroups", imports.toGroups());

        // determine the path to our marshaller file
        String mpath = source.getPath();
        mpath = mpath.replace("Service", "Marshaller");
        mpath = replacePath(mpath, "/client/", "/data/");
        writeTemplate(MARSHALLER_TMPL, mpath, ctx);

        // if we're not configured with an ActionScript source root, don't generate the
        // ActionScript versions
        if (_asroot == null || sdesc.skipAS) {
            return;
        }

        // ----------- Part II - as marshaller

        // start with the service method imports
        imports = sdesc.imports.clone();

        // add some things that marshallers just need
        imports.add(sdesc.service);
        imports.add(InvocationMarshaller.class);

        // replace inner classes with action script equivalents
        imports.translateInnerClasses();

        // ye olde special case - any method that uses a default listener
        // causes the need for the default listener marshaller
        imports.duplicateAndMunge("*.InvocationService_InvocationListener",
            "InvocationService_InvocationListener",
            "InvocationMarshaller_ListenerMarshaller",
            ".client.", ".data.");

        // any use of a listener requires the listener marshaller
        imports.pushOut("*.InvocationService_InvocationListener");
        imports.duplicateAndMunge("*Listener",
            "Service", "Marshaller",
            "Listener", "Marshaller",
            ".client.", ".data.");
        imports.popIn();

        for (ServiceMethod method : sdesc.methods) {
            method.gatherASWrappedArgListImports(imports);
        }

        // convert java bases and primitives
        ActionScriptUtils.convertBaseClasses(imports);

        // remove imports in our own package
        imports.removeSamePackage(mpackage);

        ctx.put("importGroups", imports.toGroups());

        // now generate ActionScript versions of our marshaller

        // make sure our marshaller directory exists
        String mppath = mpackage.replace('.', File.separatorChar);
        new File(_asroot + File.separator + mppath).mkdirs();

        // generate an ActionScript version of our marshaller
        String ampath = _asroot + File.separator + mppath + File.separator + mname + ".as";
        writeTemplate(AS_MARSHALLER_TMPL, ampath, ctx);

        // ----------- Part III - as listener marshallers

        Class<?> imlm = InvocationMarshaller.ListenerMarshaller.class;

        // now generate ActionScript versions of our listener marshallers
        // because those have to be in separate files
        for (ServiceListener listener : sdesc.listeners) {
            // start imports with just those used by listener methods
            imports = listener.imports.clone();

            // always need the super class and the listener class
            imports.add(imlm);
            imports.add(listener.listener);

            // replace '$' with '_' for action script naming convention
            imports.translateInnerClasses();

            // convert java bases and primitives
            ActionScriptUtils.convertBaseClasses(imports);

            // remove imports in our own package
            imports.removeSamePackage(mpackage);

            ctx.put("importGroups", imports.toGroups());
            ctx.put("listener", listener);
            String aslpath = _asroot + File.separator + mppath +
                File.separator + mname + "_" + listener.getListenerName() + "Marshaller.as";
            writeTemplate(AS_LISTENER_MARSHALLER_TMPL, aslpath, ctx);
        }

        // ----------- Part IV - as service

        // then make some changes to the context and generate ActionScript
        // versions of the service interface itself

        // start with the service methods' imports
        imports = sdesc.imports.clone();

        // add some things required by action script
        imports.add(InvocationService.class);

        // change imports of Foo$Bar to Foo_Bar
        imports.translateInnerClasses();

        // Boolean is built in
        imports.remove("boolean");

        // int is used for these
        imports.remove("byte");
        imports.remove("short");
        imports.remove("char");

        // convert java bases and primitives
        ActionScriptUtils.convertBaseClasses(imports);

        // remove imports in our own package
        imports.removeSamePackage(sdesc.spackage);

        ctx.put("importGroups", imports.toGroups());
        ctx.put("package", sdesc.spackage);

        // make sure our service directory exists
        String sppath = sdesc.spackage.replace('.', File.separatorChar);
        new File(_asroot + File.separator + sppath).mkdirs();

        // generate an ActionScript version of our service
        String aspath = _asroot + File.separator + sppath + File.separator + sname + ".as";
        writeTemplate(AS_SERVICE_TMPL, aspath, ctx);

        // ----------- Part V - as service listeners
        Class<?> isil = InvocationService.InvocationListener.class;

        // also generate ActionScript versions of any inner listener
        // interfaces because those have to be in separate files
        for (ServiceListener listener : sdesc.listeners) {
            // start with just the imports needed by listener methods
            imports = listener.imports.clone();

            // add things needed by all listeners
            imports.add(isil);
            imports.add(listener.listener);

            // change Foo$Bar to Foo_Bar
            imports.translateInnerClasses();

            ActionScriptUtils.convertBaseClasses(imports);

            // remove imports in our own package
            imports.removeSamePackage(sdesc.spackage);

            ctx.put("importGroups", imports.toGroups());
            ctx.put("listener", listener);

            String aslpath = _asroot + File.separator + sppath + File.separator +
                sname + "_" + listener.getListenerName() + "Listener.as";
            writeTemplate(AS_LISTENER_SERVICE_TMPL, aslpath, ctx);

            if (_aslistenerAdapters.contains(sname)) {
                String aslapath = _asroot + File.separator + sppath + File.separator +
                    sname + "_" + listener.getListenerName() + "ListenerAdapter.as";
                writeTemplate(AS_LISTENER_ADAPTER_SERVICE_TMPL, aslapath, ctx);
            }
        }
    }

    protected void generateDispatcher (File source, ServiceDescription sdesc)
        throws Exception
    {
        if (_verbose) {
            System.out.println("Generating dispatcher");
        }

        String name = sdesc.sname.replace("Service", "");
        String dpackage = sdesc.spackage.replace(".client", ".server");

        // start with the imports required by service methods
        ImportSet imports = sdesc.imports.clone();

        // If any listeners are to be used in dispatches, we need to import the service
        if (sdesc.listeners.size() > 0) {
            imports.add(sdesc.service);
        }

        // swap Client for ClientObject
        imports.add(sdesc.callerType);

        // add some classes required for all dispatchers
        imports.add(InvocationDispatcher.class);
        imports.add(InvocationException.class);

        // import classes contained in arrays
        imports.translateClassArrays();

        // get rid of primitives and java.lang types
        imports.removeGlobals();

        // get rid of arrays
        imports.removeArrays();

        // import the Marshaller corresponding to the service
        imports.addMunged(sdesc.service,
            "Service", "Marshaller",
            ".client.", ".data.");

        // import Foo instead of Foo$Bar
        imports.swapInnerClassesForParents();

        // remove imports in our own package
        imports.removeSamePackage(dpackage);

        // determine the path to our marshaller file
        String mpath = source.getPath();
        mpath = mpath.replace("Service", "Dispatcher");
        mpath = replacePath(mpath, "/client/", "/server/");
        writeTemplate(DISPATCHER_TMPL, mpath,
            "name", name,
            "generated", getGeneratedAnnotation(name),
            "package", dpackage,
            "methods", sdesc.methods,
            "imports", imports.toList());
    }

    protected void generateProvider (File source, ServiceDescription sdesc)
        throws Exception
    {
        if (_verbose) {
            System.out.println("Generating provider");
        }

        String name = sdesc.sname.replace("Service", "");
        String mpackage = sdesc.spackage.replace(".client", ".server");

        // start with imports required by service methods
        ImportSet imports = sdesc.imports.clone();

        if (!sdesc.methods.isEmpty()) {
            imports.add(sdesc.callerType);
        }

        // import superclass and service
        imports.add(InvocationProvider.class);
        imports.add(sdesc.service);
        imports.add("javax.annotation.Generated");

        // any method that takes a listener may throw this
        if (sdesc.hasAnyListenerArgs()) {
            imports.add(InvocationException.class);
        }

        // import classes contained in arrays
        imports.translateClassArrays();

        // get rid of primitives and java.lang types
        imports.removeGlobals();

        // get rid of arrays
        imports.removeArrays();

        // import Foo instead of Foo$Bar
        imports.swapInnerClassesForParents();

        // remove imports in our own package
        imports.removeSamePackage(mpackage);

        // determine the path to our provider file
        String mpath = source.getPath();
        mpath = mpath.replace("Service", "Provider");
        mpath = replacePath(mpath, "/client/", "/server/");
        writeTemplate(PROVIDER_TMPL, mpath,
            "name", name,
            "generated", getGeneratedAnnotation(name),
            "package", mpackage,
            "methods", sdesc.methods,
            "listeners", sdesc.listeners,
            "callerType", sdesc.callerType.getSimpleName(),
            "importGroups", imports.toGroups());
    }

    /**
     * Helper to get the appropriate "@Generated" annotation for service classes.
     */
    protected String getGeneratedAnnotation (String name)
    {
        return GenUtil.getGeneratedAnnotation(getClass(), 0, false,
            "Derived from " + name + "Service.java.");
    }

    /** Rolls up everything needed for the generate* methods. */
    protected class ServiceDescription
    {
        public Class<?> callerType = ClientObject.class;
        public boolean callerTypeSpecified;// True if callerType came from a type parameter
        public Class<?> service;
        public String sname;
        public String spackage;
        public ImportSet imports = new ImportSet();
        public List<ServiceMethod> methods = Lists.newArrayList();
        public List<ServiceListener> listeners = Lists.newArrayList();
        public final boolean skipAS;

        public ServiceDescription (Class<?> serviceClass)
        {
            service = serviceClass;
            Type[] genint = service.getGenericInterfaces();
            if (genint.length > 0 && genint[0] instanceof ParameterizedType) {
                callerType = (Class<?>)((ParameterizedType)genint[0]).getActualTypeArguments()[0];
                callerTypeSpecified = true;
            }
            sname = service.getSimpleName();
            spackage = service.getPackage().getName();
            ActionScript asa = service.getAnnotation(ActionScript.class);
            skipAS = (asa != null) && asa.omit();

            // look through and locate our service methods, also locating any
            // custom InvocationListener derivations along the way
            Method[] methdecls = service.getDeclaredMethods();
            for (Method m : methdecls) {
                // service interface methods must be public and abstract
                if (!Modifier.isPublic(m.getModifiers()) &&
                    !Modifier.isAbstract(m.getModifiers())) {
                    continue;
                }
                // check this method for custom listener declarations
                Class<?>[] args = m.getParameterTypes();
                for (Class<?> arg : args) {
                    if (_ilistener.isAssignableFrom(arg) &&
                        GenUtil.simpleName(arg).startsWith(sname + ".")) {
                        checkedAdd(listeners, new ServiceListener(service, arg));
                    }
                }
                if (_verbose) {
                    System.out.println("Adding " + m + ", imports are " +
                        StringUtil.toString(imports));
                }
                methods.add(createAndGatherImports(m, imports));
                if (_verbose) {
                    System.out.println("Added " + m + ", imports are " +
                        StringUtil.toString(imports));
                }
            }
            Collections.sort(listeners);
            Collections.sort(methods);
        }

        /**
         * Checks if any of the service method arguments are listener types.
         */
        public boolean hasAnyListenerArgs ()
        {
            return Iterables.any(methods, new Predicate<ServiceMethod>() {
                public boolean apply (ServiceMethod sm) {
                    return !sm.listenerArgs.isEmpty();
                }
            });
        }

        /**
         * Constructs a union of the imports of the service methods and all listener methods.
         */
        public ImportSet constructAllImports ()
        {
            ImportSet allimports = imports.clone();
            for (ServiceListener listener : listeners) {
                allimports.addAll(listener.imports);
            }
            return allimports;
        }
    }

    /** Show extra output if set. */
    protected boolean _verbose;

    /** The path to our ActionScript source files. */
    protected File _asroot;

    /** Services for which we should not generate provider interfaces. */
    protected Set<String> _providerless = Sets.newHashSet();

    /** Services for which we should generate actionscript listener adapters. */
    protected Set<String> _aslistenerAdapters = Sets.newHashSet();

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

    /** Specifies the path to the ActionScript listener adapter service template. */
    protected static final String AS_LISTENER_ADAPTER_SERVICE_TMPL =
        "com/threerings/presents/tools/service_listener_adapter_as.tmpl";

    /** Specifies the path to the ActionScript marshaller template. */
    protected static final String AS_MARSHALLER_TMPL =
        "com/threerings/presents/tools/marshaller_as.tmpl";

    /** Specifies the path to the ActionScript listener marshaller template. */
    protected static final String AS_LISTENER_MARSHALLER_TMPL =
        "com/threerings/presents/tools/marshaller_listener_as.tmpl";
}
