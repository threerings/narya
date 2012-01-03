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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.Project;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.primitives.Primitives;
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
import com.threerings.presents.dobj.OidList;

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
        boolean streamable = Streamable.class.isAssignableFrom(sclass) || sclass.isEnum();
        if (!streamable
            || InvocationMarshaller.class.isAssignableFrom(sclass)
            || Modifier.isInterface(sclass.getModifiers())
            || ActionScriptUtils.hasOmitAnnotation(sclass)) {
            log("Skipping " + sclass.getName(), Project.MSG_VERBOSE);
            return;
        }
        log("Generating " + sclass.getName(), Project.MSG_VERBOSE);

        // Read the existing file in, if it exists
        File outputLocation = ActionScriptUtils.createActionScriptPath(_asroot, sclass);
        String existing = null;
        if (outputLocation.exists()) {
            existing = Files.toString(outputLocation, Charsets.UTF_8);
        }

        // Generate the current version of the streamable
        StreamableClassRequirements reqs = new StreamableClassRequirements(sclass);

        ImportSet imports = new ImportSet();
        String extendsName = "";
        if (sclass.isEnum()) {
            imports.add("com.threerings.util.Enum");
            extendsName = "Enum";
        } else {
            imports.add(ObjectInputStream.class.getName());
            imports.add(ObjectOutputStream.class.getName());
        }
        if (!sclass.getSuperclass().equals(Object.class)) {
            extendsName =
                ActionScriptUtils.addImportAndGetShortType(sclass.getSuperclass(), false, imports);
        }

        // Read the existing file's imports in. Eclipse's import organization makes
        // it a pain to keep additional imports out of the GENERATED PREAMBLE section of
        // class, so we just merge all the imports we find.
        if (existing != null) {
            ActionScriptUtils.addExistingImports(existing, imports);
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
        List<ASEnum> enumFields = Lists.newArrayList();
        if (sclass.isEnum()) {
            Object[] enums = sclass.getEnumConstants();
            for (Object e : enums) {
                enumFields.add(new ASEnum((Enum<?>)e));
            }
        }

        imports.removeGlobals();

        String template = sclass.isEnum() ? "enum_as.tmpl" : "streamable_as.tmpl";
        String output = mergeTemplate("com/threerings/presents/tools/" + template,
            "header", existing == null ? _header : "",
            "package", sclass.getPackage().getName(),
            "classname", ActionScriptUtils.toSimpleName(sclass),
            "importGroups", imports.toGroups(),
            "extends", extendsName,
            "implements", Joiner.on(", ").join(implemented),
            "superclassStreamable", reqs.superclassStreamable,
            "pubFields", pubFields,
            "enumFields", enumFields,
            "protFields", protFields,
            "dobject", isDObject);

        if (existing != null) {
            // Merge in the previously generated version
            output = new GeneratedSourceMerger().merge(output, existing);
        }
        writeFile(outputLocation.getAbsolutePath(), output);

        // generate inner enums
        for (Class<?> inner : sclass.getDeclaredClasses()) {
            if (inner.isEnum()) {
                processClass(javaSource, inner);
            }
        }
    }

    protected static class ASField
    {
        public final String name;
        public final String capitalName;
        public final String simpleType;
        public final String parameterTypes;
        public final String reader;
        public final String writer;
        public final String dobjectField;
        public boolean dset;
        public boolean array;
        public boolean oidList;
        public boolean hasTypeParameters;

        public ASField (Field f, ImportSet imports)
        {
            name = f.getName();
            capitalName = StringUtil.capitalize(name);
            dobjectField = StringUtil.unStudlyName(name).toUpperCase();
            simpleType = ActionScriptUtils.addImportAndGetShortType(f.getType(), true, imports);

            List<String> parameters = Lists.newLinkedList();
            Type genType = f.getGenericType();
            if (genType instanceof ParameterizedType) {
                for (Type param : ((ParameterizedType) genType).getActualTypeArguments()) {
                    if (param instanceof Class) {
                        // Convert any box classes to primitives to avoid having to import them
                        Class<?> primitive = Primitives.unwrap((Class<?>) param);
                        parameters.add(ActionScriptUtils.addImportAndGetShortType(
                            primitive, false, imports));
                    }
                }
            }
            if (!parameters.isEmpty()) {
                parameterTypes = Joiner.on(", ").join(parameters);
                hasTypeParameters = true;
            } else {
                parameterTypes = "";
            }

            // Lists and Maps use their Streamers directly
            if (List.class.isAssignableFrom(f.getType())) {
                imports.add("com.threerings.io.streamers.ArrayStreamer");
            }  else if (Map.class.isAssignableFrom(f.getType())) {
                imports.add("com.threerings.io.streamers.MapStreamer");
            } else if (Set.class.isAssignableFrom(f.getType())) {
                imports.add("com.threerings.io.streamers.SetStreamer");
            } else if (DSet.class.isAssignableFrom(f.getType())) {
                dset = true;
            } else if (OidList.class.isAssignableFrom(f.getType())) {
                oidList = true;
            }
            array = f.getType().isArray();
            reader = ActionScriptUtils.toReadObject(f.getType());
            writer = ActionScriptUtils.toWriteObject(f.getType(), name);
        }
    }

    protected static class ASEnum
    {
        public final String name;

        public ASEnum (Enum<?> e)
        {
            name = e.name();
        }
    }

    /** The path to our ActionScript source files. */
    protected File _asroot;
}
