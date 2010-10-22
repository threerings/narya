//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Iterator;
import java.util.List;

import java.io.File;

import com.google.common.collect.Iterators;

import com.samskivert.util.ComparableArrayList;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.InvocationDecoder;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;

/**
 * An Ant task for generating invocation receiver marshalling and unmarshalling classes.
 */
public class GenReceiverTask extends InvocationTask
{
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
        if (!rname.endsWith("Receiver")) {
            System.err.println("Cannot process '" + rname + "':");
            System.err.println("Receiver classes must be named SomethingReceiver.");
            return;
        }

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
            methods.add(new ServiceMethod(m, imports));
        }
        methods.sort();

        // import Foo instead of Foo$Bar
        imports.swapInnerClassesForParents();

        // Adjust any bits that want to import arrays to instead import their element classes.
        imports.translateClassArrays();
        imports.removeArrays();

        // get rid of primitives and java.lang types
        imports.removeGlobals();

        generateSender(source, rname, rpackage, methods, imports.toList().iterator());
        generateDecoder(receiver, source, rname, rpackage, methods, imports.toList().iterator(),
            StringUtil.md5hex(rpackage + "." + rname));
    }

    protected void generateSender (File source, String rname, String rpackage,
                                   List<?> methods, Iterator<String> imports)
        throws Exception
    {
        String name = rname.replace("Receiver", "");
        String spackage = rpackage.replace(".client", ".server");

        // construct our imports list
        ComparableArrayList<String> implist = new ComparableArrayList<String>();
        Iterators.addAll(implist, imports);
        checkedAdd(implist, ClientObject.class.getName());
        checkedAdd(implist, InvocationSender.class.getName());
        String dname = rname.replace("Receiver", "Decoder");
        checkedAdd(implist, rpackage + "." + dname);
        checkedAdd(implist, rpackage + "." + rname);
        implist.sort();

        // determine the path to our sender file
        String mpath = source.getPath();
        mpath = mpath.replace("Receiver", "Sender");
        mpath = replacePath(mpath, "/client/", "/server/");
        writeFile(mpath, mergeTemplate(SENDER_TMPL,
                                       "name", name,
                                       "package", spackage,
                                       "methods", methods,
                                       "imports", implist));
    }

    protected void generateDecoder (Class<?> receiver, File source, String rname, String rpackage,
                                    List<ServiceMethod> methods, Iterator<String> imports,
                                    String rcode) throws Exception
    {
        String name = rname.replace("Receiver", "");

        // construct our imports list
        ComparableArrayList<String> implist = new ComparableArrayList<String>();
        Iterators.addAll(implist, imports);
        checkedAdd(implist, InvocationDecoder.class.getName());
        implist.sort();

        // determine the path to our sender file
        String mpath = source.getPath();
        mpath = mpath.replace("Receiver", "Decoder");
        writeFile(mpath, mergeTemplate(DECODER_TMPL,
                                       "name", name,
                                       "receiver_code", rcode,
                                       "package", rpackage,
                                       "methods", methods,
                                       "imports", implist));
    }

    /** Specifies the path to the sender template. */
    protected static final String SENDER_TMPL =
        "com/threerings/presents/tools/sender.tmpl";

    /** Specifies the path to the decoder template. */
    protected static final String DECODER_TMPL =
        "com/threerings/presents/tools/decoder.tmpl";
}
