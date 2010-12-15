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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;

import org.apache.tools.ant.Project;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

public class GenActionScriptStreamableTask extends GenTask
{
    /**
     * Configures the path to our ActionScript source files.
     */
    public void setAsroot (File asroot)
    {
        _asroot = asroot;
    }

    @Override
    protected void processClass (File javaSource, Class<?> sclass)
        throws Exception
    {
        if (!Streamable.class.isAssignableFrom(sclass)
            || InvocationMarshaller.class.isAssignableFrom(sclass)
            || Modifier.isInterface(sclass.getModifiers())
            || ActionScriptUtils.hasOmitAnnotation(sclass)) {
            log("Skipping " + sclass.getName(), Project.MSG_VERBOSE);
            return;
        }
        log("Generating " + sclass.getName(), Project.MSG_VERBOSE);
        // Generate the current version of the streamable
        StreamableClassRequirements reqs = new StreamableClassRequirements(sclass);

        Set<String> imports = Sets.newLinkedHashSet();
        imports.add(ObjectInputStream.class.getName());
        imports.add(ObjectOutputStream.class.getName());
        String extendsName = "";
        if (!sclass.getSuperclass().equals(Object.class)) {
            extendsName =
                ActionScriptUtils.addImportAndGetShortType(sclass.getSuperclass(), false, imports);
        }

        boolean isDObject = DObject.class.isAssignableFrom(sclass);
        if (isDObject) {
            imports.add("org.osflash.signals.Signal");
        }

        Set<String> implemented = Sets.newLinkedHashSet();
        for (Class<?> iface : sclass.getInterfaces()) {
            implemented.add(ActionScriptUtils.addImportAndGetShortType(iface, false, imports));
        }
        List<ASField> pubFields = Lists.newArrayList();
        List<ASField> protFields = Lists.newArrayList();
        for (Field f : reqs.streamedFields) {
            int mods = f.getModifiers();
            if (Modifier.isPublic(mods)) {
                pubFields.add(new ASField(f, imports));
            } else if (Modifier.isProtected(mods)) {
                protFields.add(new ASField(f, imports));
            } else {
                // don't care about private
                continue;
            }
        }

        String output = mergeTemplate("com/threerings/presents/tools/streamable_as.tmpl",
            "header", _header,
            "package", sclass.getPackage().getName(),
            "classname", sclass.getSimpleName(),
            "imports", imports,
            "extends", extendsName,
            "implements", Joiner.on(", ").join(implemented),
            "superclassStreamable", reqs.superclassStreamable,
            "pubFields", pubFields,
            "protFields", protFields,
            "dobject", isDObject);

        File outputLocation = ActionScriptUtils.createActionScriptPath(_asroot, sclass);
        if (outputLocation.exists()) {
            // Merge in the previously generated version
            String existing = Files.toString(outputLocation, Charsets.UTF_8);
            output = new GeneratedSourceMerger().merge(output, existing);
        }
        writeFile(outputLocation.getAbsolutePath(), output);
    }

    protected static class ASField
    {
        public final String name;
        public final String capitalName;
        public final String simpleType;
        public final String reader;
        public final String writer;
        public final String dobjectField;
        public boolean dset;
        public boolean array;

        public ASField (Field f, Set<String> imports)
        {
            name = f.getName();
            capitalName = StringUtil.capitalize(name);
            dobjectField = StringUtil.unStudlyName(name).toUpperCase();
            simpleType = ActionScriptUtils.addImportAndGetShortType(f.getType(), true, imports);

            // Lists and Maps use their Streamers directly
            if (List.class.isAssignableFrom(f.getType())) {
                imports.add("com.threerings.io.streamers.ArrayStreamer");
            }  else if (Map.class.isAssignableFrom(f.getType())) {
                imports.add("com.threerings.io.streamers.MapStreamer");
            } else if (Set.class.isAssignableFrom(f.getType())) {
                imports.add("com.threerings.io.streamers.SetStreamer");
            } else if (DSet.class.isAssignableFrom(f.getType())) {
                dset = true;
                imports.add("com.threerings.presents.dobj.DSet_Entry"); // Used for signals
            }
            array = f.getType().isArray();
            reader = ActionScriptUtils.toReadObject(f.getType());
            writer = ActionScriptUtils.toWriteObject(f.getType(), name);
        }
    }

    /** The path to our ActionScript source files. */
    protected File _asroot;
}
