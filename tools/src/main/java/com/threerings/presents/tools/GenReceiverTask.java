//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.tools;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.List;

import java.io.File;

import com.google.common.base.Preconditions;

import com.samskivert.util.ComparableArrayList;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.InvocationDecoder;
import com.threerings.presents.client.InvocationReceiver;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;

/**
 * An Ant task for generating invocation receiver marshalling and unmarshalling classes.
 */
public class GenReceiverTask extends InvocationTask
{
    /**
     * Configures the path to our ActionScript source files.
     */
    public void setAsroot (File asroot)
    {
        _asroot = asroot;
    }

    @Override
    public void processClass (File source, Class<?> receiver)
        throws Exception
    {
        System.out.println("Processing " + receiver.getName() + "...");
        String rname = receiver.getName();
        String rpackage = "";
        int didx = rname.lastIndexOf(".");
        if (didx != -1) {
            rpackage = rname.substring(0, didx);
            rname = rname.substring(didx+1);
        }

        // verify that the receiver class name is as we expect it to be
        Preconditions.checkArgument(rname.endsWith("Receiver"), "Cannot process '%s'. " +
            "Receiver classes must be named SomethingReceiver.", rname);

        ImportSet imports = new ImportSet();
        ComparableArrayList<ServiceMethod> methods = new ComparableArrayList<ServiceMethod>();

        // look through and locate our receiver methods
        Method[] methdecls = receiver.getDeclaredMethods();
        for (Method m : methdecls) {
            // receiver interface methods must be public and abstract
            if (!Modifier.isPublic(m.getModifiers()) &&
                !Modifier.isAbstract(m.getModifiers())) {
                continue;
            }
            methods.add(createAndGatherImports(m, imports));
        }
        methods.sort();

        // import Foo instead of Foo$Bar
        imports.swapInnerClassesForParents();

        // Adjust any bits that want to import arrays to instead import their element classes.
        imports.translateClassArrays();
        imports.removeArrays();

        // get rid of primitives and java.lang types
        imports.removeGlobals();

        generateSender(source, rname, rpackage, methods, imports);
        generateDecoder(receiver, source, rname, rpackage, methods, imports,
            StringUtil.md5hex(rpackage + "." + rname));
    }

    protected void generateSender (File source, String rname, String rpackage,
                                   List<?> methods, ImportSet imports)
        throws Exception
    {
        String name = rname.replace("Receiver", "");
        String spackage = rpackage.replace(".client", ".server");

        // construct our imports list
        ImportSet impset = new ImportSet();
        impset.addAll(imports);
        impset.add(ClientObject.class);
        impset.add(InvocationSender.class);
        String dname = rname.replace("Receiver", "Decoder");
        impset.add(rpackage + "." + dname);
        impset.add(rpackage + "." + rname);

        // determine the path to our sender file
        String mpath = source.getPath();
        mpath = mpath.replace("Receiver", "Sender");
        mpath = replacePath(mpath, "/client/", "/server/");
        writeTemplate(SENDER_TMPL, mpath,
            "name", name,
            "package", spackage,
            "methods", methods,
            "importGroups", impset.toGroups());
    }

    protected void generateDecoder (Class<?> receiver, File source, String rname, String rpackage,
                                    List<ServiceMethod> methods, ImportSet imports,
                                    String rcode) throws Exception
    {
        String name = rname.replace("Receiver", "");

        // construct our imports list
        ImportSet impset = new ImportSet();
        impset.addAll(imports);
        impset.add(InvocationDecoder.class);

        // determine the path to our sender file
        String mpath = source.getPath();
        mpath = mpath.replace("Receiver", "Decoder");
        writeTemplate(DECODER_TMPL, mpath,
            "name", name,
            "receiver_code", rcode,
            "package", rpackage,
            "methods", methods,
            "importGroups", impset.toGroups());
        if (_asroot == null) {
            return;
        }
        // generate an ActionScript decoder
        String sppath = rpackage.replace('.', File.separatorChar);
        String aspath = _asroot + File.separator + sppath + File.separator + name + "Decoder.as";
        writeTemplate(AS_DECODER_TMPL, aspath,
            "name", name,
            "receiver_code", rcode,
            "package", rpackage,
            "methods", methods,
            "importGroups", impset.toGroups());

        // ... and an ActionScript receiver
        aspath = _asroot + File.separator + sppath + File.separator + rname + ".as";
        impset.remove(InvocationDecoder.class);
        impset.add(InvocationReceiver.class);
        writeTemplate(AS_RECEIVER_TMPL, aspath,
            "name", name,
            "package", rpackage,
            "methods", methods,
            "importGroups", impset.toGroups());
    }

    /** The path to our ActionScript source files. */
    protected File _asroot;

    /** Specifies the path to the sender template. */
    protected static final String SENDER_TMPL =
        "com/threerings/presents/tools/sender.tmpl";

    /** Specifies the path to the decoder template. */
    protected static final String DECODER_TMPL =
        "com/threerings/presents/tools/decoder.tmpl";

    /** Specifies the path to the decoder template. */
    protected static final String AS_DECODER_TMPL =
        "com/threerings/presents/tools/decoder_as.tmpl";

    /** Specifies the path to the decoder template. */
    protected static final String AS_RECEIVER_TMPL =
        "com/threerings/presents/tools/receiver_as.tmpl";
}
