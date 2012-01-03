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

package com.threerings.presents.tools.cpp;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.threerings.io.SimpleStreamableObject;
import com.threerings.io.Streamable;

import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.net.Message;
import com.threerings.presents.tools.GenTask;

import static com.threerings.presents.tools.cpp.CPPUtil.makeCPPName;
import static com.threerings.presents.tools.cpp.CPPUtil.makeNamespaces;
import static com.threerings.presents.tools.cpp.CPPUtil.makePath;

public class GenCPPStreamableTask extends GenTask
{
    public class Generate {
        public void setClass (String name)
        {
            _toProcess.add(loadClass(name));
        }
    }

    public Generate createGenerate ()
    {
        return new Generate();
    }

    public void setCpproot (File asroot)
    {
        _cpproot = asroot;
    }

    @Override
    public void execute ()
    {
        for (Class<?> klass : _toProcess) {
            processClass(null, klass);
        }
        super.execute();
    }

    @Override
    protected void processClass (File fn, Class<?> sclass)
    {
        try {
            processClass(sclass);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Processes a resolved Streamable class instance.
     */
    protected void processClass (Class<?> sclass)
        throws IOException
    {
        if (!Streamable.class.isAssignableFrom(sclass) ||
            ((sclass.getModifiers() & Modifier.INTERFACE) != 0) ||
            DSet.class.equals(sclass) ||
            (InvocationMarshaller.class.isAssignableFrom(sclass) && !InvocationMarshaller.class.equals(sclass))) {
//            System.err.println("Skipping " + sclass.getName() + "...");
            return;
        }

        System.err.println("Generating " + sclass.getName());

        // see if our parent also implements Streamable
        boolean needSuper = Streamable.class.isAssignableFrom(sclass.getSuperclass()) &&
             !NONSUPER.contains(sclass.getSuperclass());

        Map<String, Object> ctx = Maps.newHashMap();
        ctx.put("superclassStreamable", needSuper);
        ctx.put("name", sclass.getSimpleName());
        ctx.put("namespaces", makeNamespaces(sclass));
        ctx.put("javaName", sclass.getName());
        ctx.put("namespace", CPPUtil.makeNamespace(sclass));

        Set<String> headerIncludes = Sets.newTreeSet();
        Set<String> implIncludes = Sets.newTreeSet();
        if (needSuper) {
            ctx.put("super", makeCPPName(sclass.getSuperclass()));
            addInclude(sclass.getSuperclass(), headerIncludes);
        } else {
            ctx.put("super", "Streamable");
            headerIncludes.add("presents/Streamable.h");
        }

        List<CPPField> fields = Lists.newArrayList();
        ctx.put("fields", fields);
        for (Field field : sclass.getDeclaredFields()) {
            int mods = field.getModifiers();
            if (!Modifier.isStatic(mods) && !Modifier.isTransient(mods)) {
                CPPField cppField = new CPPField(field);
                fields.add(cppField);
                CPPType type = cppField.type;
                while (type != null) {
                    if (CPPType.JAVA_LIST_FIXED.equals(type.fixed)) {
                        implIncludes.add("presents/Streamer.h");
                    }
                    if (type.representationImport != null) {
                        headerIncludes.add(type.representationImport);
                    }
                    type = type.dependent;
                }
            }
        }
        String inStream, outStream;
        if (fields.isEmpty() && !needSuper) {
            inStream = "/*in*/";
            outStream = "/*out*/";
        } else {
            inStream = "in";
            outStream = "out";

        }
        ctx.put("inStreamArg", inStream);
        ctx.put("outStreamArg", outStream);

        // now write all that out to the target source file
        ctx.put("includes", headerIncludes);
        writeTemplate(HEADER_TMPL, makePath(_cpproot, sclass, ".h"), ctx);

        ctx.put("includes", implIncludes);
        writeTemplate(CPP_TMPL, makePath(_cpproot, sclass, ".cpp"), ctx);
    }

    protected static void addInclude (Class<?> ftype, Set<String> includes)
    {
        includes.add(makePath(ftype, ".h"));
    }

    protected File _cpproot;

    protected Set<Class<?>> _toProcess = Sets.newHashSet();

    protected static final Set<Class<?>> NONSUPER = Sets.newHashSet();
    static {
        NONSUPER.add(Message.class);
        NONSUPER.add(SimpleStreamableObject.class);
    }
    protected static final String HEADER_TMPL =
        "com/threerings/presents/tools/cpp/streamable_h.mustache";
    protected static final String CPP_TMPL =
        "com/threerings/presents/tools/cpp/streamable_cpp.mustache";
}
