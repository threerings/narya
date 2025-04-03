//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.tools.cpp;

import static com.threerings.presents.tools.cpp.CPPUtil.makePath;

import java.lang.reflect.Type;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.threerings.presents.tools.GenReceiverTask;
import com.threerings.presents.tools.ImportSet;

public class GenCPPReceiverTask extends GenReceiverTask
{
    public void setCpproot (File asroot)
    {
        _cpproot = asroot;
    }

    @Override
    protected void generateDecoder (Class<?> receiver, File source, String rname, String rpackage,
        List<ServiceMethod> methods, ImportSet imports, String rcode)
        throws Exception
    {
        String dname = rname.replace("Receiver", "Decoder");

        Map<String, Object> ctx = Maps.newHashMap();
        List<String> namespaces = CPPUtil.makeNamespaces(receiver);
        ctx.put("receiverName", rname);
        ctx.put("decoderName", dname);
        ctx.put("namespaces", namespaces);
        ctx.put("namespace", CPPUtil.makeNamespace(receiver));
        ctx.put("package", rpackage);
        ctx.put("methods", MethodDescriptor.from(methods));
        ctx.put("receiverCode", rcode);
        ctx.put("argbuilder", new CPPArgBuilder());

        writeTemplate(DECODER_HEADER_TMPL, makePath(_cpproot, namespaces, dname, ".h"), ctx);

        Set<String> receiverHeaderIncludes = Sets.newTreeSet();
        Set<String> decoderImplIncludes = Sets.newTreeSet();
        for (ServiceMethod meth : methods) {
            for (Type type : meth.method.getGenericParameterTypes()) {
                CPPType cppType = new CPPType(type);
                if (cppType.primitive) {
                    decoderImplIncludes.add("presents/box/Boxed" + cppType.interpreter + ".h");
                }
                while (cppType != null) {
                    if (cppType.representationImport != null) {
                        receiverHeaderIncludes.add(cppType.representationImport);
                    }
                    cppType = cppType.dependent;
                }
            }
        }

        ctx.put("includes", decoderImplIncludes);
        writeTemplate(DECODER_CPP_TMPL, makePath(_cpproot, namespaces, dname, ".cpp"), ctx);
        ctx.put("includes", receiverHeaderIncludes);
        writeTemplate(RECEIVER_HEADER_TMPL, makePath(_cpproot, namespaces, rname, ".h"), ctx);

        super.generateDecoder(receiver, source, rname, rpackage, methods, imports, rcode);
    }

    protected File _cpproot;

    protected static final String RECEIVER_HEADER_TMPL =
        "com/threerings/presents/tools/cpp/receiver_h.mustache";
    protected static final String DECODER_HEADER_TMPL =
        "com/threerings/presents/tools/cpp/decoder_h.mustache";
    protected static final String DECODER_CPP_TMPL =
        "com/threerings/presents/tools/cpp/decoder_cpp.mustache";
}
