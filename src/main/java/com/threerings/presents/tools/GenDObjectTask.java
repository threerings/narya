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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import com.samskivert.util.StringUtil;
import com.threerings.presents.annotation.TransportHint;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.OidList;

/**
 * Generates necessary additional distributed object declarations and methods.
 */
public class GenDObjectTask extends GenTask
{
    @Override
    public void execute ()
    {
        // resolve the DObject class using our classloader
        _doclass = loadClass(DObject.class.getName());
        _dsclass = loadClass(DSet.class.getName());
        _olclass = loadClass(OidList.class.getName());

        super.execute();
    }

    /** Processes a resolved distributed object class instance. */
    @Override
    public void processClass (File source, Class<?> oclass)
        throws Exception
    {
        // make sure we extend distributed object
        if (!_doclass.isAssignableFrom(oclass) || _doclass.equals(oclass)) {
            // System.err.println("Skipping " + oclass.getName() + "...");
            return;
        }

        // determine which fields we need to deal with
        ArrayList<Field> flist = Lists.newArrayList();
        Field[] fields = oclass.getDeclaredFields();
        for (Field f : fields) {
            int mods = f.getModifiers();
            if (!Modifier.isPublic(mods) ||
                Modifier.isStatic(mods) ||
                Modifier.isTransient(mods)) {
                continue;
            }
            flist.add(f);
        }

        // slurp our source file into newline separated strings
        SourceFile sfile = new SourceFile();
        sfile.readFrom(source);

        // generate our fields section and our methods section
        StringBuilder fsection = new StringBuilder();
        StringBuilder msection = new StringBuilder();
        for (int ii = 0; ii < flist.size(); ii++) {
            Field f = flist.get(ii);
            Class<?> ftype = f.getType();
            String fname = f.getName();

            // create a map to hold our template data
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("field", fname);
            data.put("generated", GenUtil.getGeneratedAnnotation(getClass(), 4, false));
            data.put("type", GenUtil.simpleName(f));
            data.put("wrapfield", GenUtil.boxArgument(ftype, "value"));
            data.put("wrapofield", GenUtil.boxArgument(ftype, "ovalue"));
            data.put("clonefield", GenUtil.cloneArgument(_dsclass, f, "value"));
            data.put("capfield", StringUtil.unStudlyName(fname).toUpperCase());
            data.put("upfield", StringUtil.capitalize(fname));

            // determine the type of transport
            TransportHint hint = f.getAnnotation(TransportHint.class);
            if (hint == null) {
                // inherit hint from class annotation
                hint = f.getDeclaringClass().getAnnotation(TransportHint.class);
            }
            String transport;
            if (hint == null) {
                transport = "";
            } else {
                transport = ",\n" +
                    "            com.threerings.presents.net.Transport.getInstance(\n" +
                    "                com.threerings.presents.net.Transport.Type." +
                        hint.type().name() + ", " + hint.channel() + ")";
            }
            data.put("transport", transport);

            // if this field is an array, we need its component types
            boolean array = ftype.isArray();
            data.put("have_elem", array);
            if (array) {
                Class<?> etype = ftype.getComponentType();
                data.put("elemtype", GenUtil.simpleName(etype));
                data.put("wrapelem", GenUtil.boxArgument(etype, "value"));
                data.put("wrapoelem", GenUtil.boxArgument(etype, "ovalue"));
            }

            // if this field is a generic DSet, we need its bound type
            if (_dsclass.isAssignableFrom(ftype)) {
                Type t = f.getGenericType();
                // we need to walk up the heirarchy until we get to the parameterized DSet
                while (t instanceof Class<?>) {
                    t = ((Class<?>)t).getGenericSuperclass();
                }
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)t;
                    if (pt.getActualTypeArguments().length > 0) {
                        data.put("etype", GenUtil.simpleName(pt.getActualTypeArguments()[0]));
                    }
                } else {
                    data.put("etype", "DSet.Entry");
                }
            }

            // now figure out which template to use
            String tname = "field.tmpl";
            if (_dsclass.isAssignableFrom(ftype)) {
                tname = "set.tmpl";
            } else if (_olclass.isAssignableFrom(ftype)) {
                tname = "oidlist.tmpl";
            }

            // append the merged templates as appropriate to the string buffers
            if (ii > 0) {
                fsection.append(EOL);
                msection.append(EOL);
            }
            fsection.append(mergeTemplate(NAME_TMPL, data));
            msection.append(mergeTemplate(BASE_TMPL + tname, data));
        }

        // now bolt everything back together into a class declaration
        writeFile(source.getAbsolutePath(), sfile.generate(fsection.toString(), msection.toString()));
    }

    /** {@link DObject} resolved with the proper classloader so that we
     * can compare it to loaded derived classes. */
    protected Class<?> _doclass;

    /** {@link DSet} resolved with the proper classloader so that we can
     * compare it to loaded derived classes. */
    protected Class<?> _dsclass;

    /** {@link OidList} resolved with the proper classloader so that we
     * can compare it to loaded derived classes. */
    protected Class<?> _olclass;

    /** Specifies the start of the path to our various templates. */
    protected static final String BASE_TMPL =
        "com/threerings/presents/tools/dobject_";

    /** Specifies the path to the name code template. */
    protected static final String NAME_TMPL = BASE_TMPL + "name.tmpl";
}
