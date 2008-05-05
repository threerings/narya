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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.VelocityContext;

import com.samskivert.util.CollectionUtil;
import com.samskivert.util.ComparableArrayList;
import com.samskivert.util.StringUtil;

import com.threerings.presents.client.InvocationDecoder;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationSender;

/**
 * An Ant task for generating invocation receiver marshalling and unmarshalling
 * classes.
 */
public class GenReceiverTask extends InvocationTask
{
    // documentation inherited
    protected void processService (File source, Class receiver)
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
            System.err.println(
                "Receiver classes must be named SomethingReceiver.");
            return;
        }

        HashMap<String,Boolean> imports = new HashMap<String,Boolean>();
        ComparableArrayList<ServiceMethod> methods =
            new ComparableArrayList<ServiceMethod>();

        // we need to import the receiver itself
        imports.put(importify(receiver.getName()), Boolean.TRUE);

        // look through and locate our receiver methods
        Method[] methdecls = receiver.getDeclaredMethods();
        for (int ii = 0; ii < methdecls.length; ii++) {
            Method m = methdecls[ii];
            // receiver interface methods must be public and abstract
            if (!Modifier.isPublic(m.getModifiers()) &&
                !Modifier.isAbstract(m.getModifiers())) {
                continue;
            }
            methods.add(new ServiceMethod(receiver, m, imports, null, 0, true));
        }
        methods.sort();

        generateSender(source, rname, rpackage, methods,
                       imports.keySet().iterator());
        generateDecoder(source, rname, rpackage, methods,
                        imports.keySet().iterator());
    }

    protected void generateSender (File source, String rname, String rpackage,
                                   List methods, Iterator<String> imports)
    {
        String name = StringUtil.replace(rname, "Receiver", "");
        String spackage = StringUtil.replace(rpackage, ".client", ".server");

        // construct our imports list
        ComparableArrayList<String> implist = new ComparableArrayList<String>();
        CollectionUtil.addAll(implist, imports);
        checkedAdd(implist, ClientObject.class.getName());
        checkedAdd(implist, InvocationSender.class.getName());
        String dname = StringUtil.replace(rname, "Receiver", "Decoder");
        checkedAdd(implist, rpackage + "." + dname);
        implist.sort();

        VelocityContext ctx = new VelocityContext();
        ctx.put("name", name);
        ctx.put("package", spackage);
        ctx.put("methods", methods);
        ctx.put("imports", implist);

        try {
            StringWriter sw = new StringWriter();
            _velocity.mergeTemplate(SENDER_TMPL, "UTF-8", ctx, sw);

            // determine the path to our sender file
            String mpath = source.getPath();
            mpath = StringUtil.replace(mpath, "Receiver", "Sender");
            mpath = replacePath(mpath, "/client/", "/server/");

            writeFile(mpath, sw.toString());

        } catch (Exception e) {
            System.err.println("Failed processing template");
            e.printStackTrace(System.err);
        }
    }

    protected void generateDecoder (
        File source, String rname, String rpackage, List methods,
        Iterator<String> imports)
    {
        String name = StringUtil.replace(rname, "Receiver", "");

        // construct our imports list
        ComparableArrayList<String> implist = new ComparableArrayList<String>();
        CollectionUtil.addAll(implist, imports);
        checkedAdd(implist, InvocationDecoder.class.getName());
        implist.sort();

        VelocityContext ctx = new VelocityContext();
        ctx.put("name", name);
        ctx.put("receiver_code", StringUtil.md5hex(rpackage + "." + rname));
        ctx.put("package", rpackage);
        ctx.put("methods", methods);
        ctx.put("imports", implist);

        try {
            StringWriter sw = new StringWriter();
            _velocity.mergeTemplate(DECODER_TMPL, "UTF-8", ctx, sw);

            // determine the path to our sender file
            String mpath = source.getPath();
            mpath = StringUtil.replace(mpath, "Receiver", "Decoder");

            writeFile(mpath, sw.toString());

        } catch (Exception e) {
            System.err.println("Failed processing template");
            e.printStackTrace(System.err);
        }
    }

    /** Specifies the path to the sender template. */
    protected static final String SENDER_TMPL =
        "com/threerings/presents/tools/sender.tmpl";

    /** Specifies the path to the decoder template. */
    protected static final String DECODER_TMPL =
        "com/threerings/presents/tools/decoder.tmpl";
}
