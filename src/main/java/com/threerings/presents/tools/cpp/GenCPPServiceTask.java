package com.threerings.presents.tools.cpp;

import java.lang.reflect.Type;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.threerings.presents.tools.GenServiceTask;

import static com.threerings.presents.tools.cpp.CPPUtil.makeNamespaces;
import static com.threerings.presents.tools.cpp.CPPUtil.makePath;

public class GenCPPServiceTask extends GenServiceTask
{
    public void setCpproot (File asroot)
    {
        _cpproot = asroot;
    }

    @Override
    protected void generateMarshaller (File source, ServiceDescription sdesc)
        throws Exception
    {
        String name = sdesc.sname.replace("Service", "Marshaller");
        String mpackage = sdesc.spackage.replace(".client", ".data");

        List<String> namespaces = makeNamespaces(mpackage);
        Map<String, Object> ctx = Maps.newHashMap();
        ctx.put("name", name);
        ctx.put("javaName", mpackage + "." + name);
        ctx.put("namespaces", namespaces);
        ctx.put("namespace", Joiner.on("::").join(namespaces));
        ctx.put("methods",  MethodDescriptor.from(sdesc.methods));
        ctx.put("listeners", sdesc.listeners);
        ctx.put("argbuilder", new CPPArgBuilder(true));

        Set<String> includes = Sets.newTreeSet();
        Set<String> implIncludes = Sets.newTreeSet();
        for (ServiceMethod meth : sdesc.methods) {
            for (Type type : meth.method.getGenericParameterTypes()) {
                CPPType cppType = new CPPType(type);
                if (cppType.primitive) {
                    implIncludes.add("presents/box/Boxed" + cppType.interpreter + ".h");
                }
                while (cppType != null) {
                    if (cppType.representationImport != null) {
                        includes.add(cppType.representationImport);
                    }
                    cppType = cppType.dependent;
                }
            }
        }
        ctx.put("includes", implIncludes);
        writeTemplate(CPP_TMPL, makePath(_cpproot, namespaces, name, ".cpp"), ctx);
        ctx.put("includes", includes);
        writeTemplate(HEADER_TMPL, makePath(_cpproot, namespaces, name, ".h"), ctx);
        super.generateMarshaller(source, sdesc);
    }

    protected File _cpproot;

    protected static final String HEADER_TMPL =
        "com/threerings/presents/tools/cpp/marshaller_h.mustache";
    protected static final String CPP_TMPL =
        "com/threerings/presents/tools/cpp/marshaller_cpp.mustache";
}
