package com.threerings.presents.tools.cpp;

import java.lang.reflect.Type;

import java.util.List;

import com.google.common.collect.Lists;

import com.threerings.presents.tools.InvocationTask.ServiceMethod;

public class CPPArgBuilder
{
    public CPPArgBuilder(boolean ignoreFirst)
    {
        _ignoreFirst = ignoreFirst;
    }

    public String getArguments (ServiceMethod meth)
    {
        return getArguments(meth, "");
    }

    public String getArguments (ServiceMethod meth, String prefix)
    {
        StringBuilder buf = new StringBuilder(prefix);
        Type[] ptypes = meth.method.getGenericParameterTypes();
        for (int ii = _ignoreFirst ? 1 : 0; ii < ptypes.length; ii++) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(CPPUtil.getCPPType(ptypes[ii])).append(" arg").append(ii);
        }
        return buf.toString();
    }

    public List<String> getArgumentNames (ServiceMethod meth)
    {
        Type[] ptypes = meth.method.getGenericParameterTypes();
        List<String> args = Lists.newArrayListWithCapacity(ptypes.length - 1);
        for (int ii = 1; ii < ptypes.length; ii++) {
            args.add("arg" + ii);
        }
        return args;
    }

    public String getArgumentsFromVector (ServiceMethod meth)
    {
        StringBuilder buf = new StringBuilder();
        Type[] ptypes = meth.method.getGenericParameterTypes();
        for (int ii = _ignoreFirst ? 1 : 0; ii < ptypes.length; ii++) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            CPPType type = new CPPType(ptypes[ii]);
            buf.append(type.getCastFromStreamable("args[" + ii + "]"));
        }
        return buf.toString();
    }

    public List<String> getServiceArguments (ServiceMethod meth)
    {
        Type[] ptypes = meth.method.getGenericParameterTypes();
        List<String> args = Lists.newArrayListWithCapacity(ptypes.length);
        for (int ii = _ignoreFirst ? 1 : 0; ii < ptypes.length; ii++) {
            args.add(new CPPType(ptypes[ii]).getAsStreamable("arg" + ii));
        }
        return args;
    }

    protected final boolean _ignoreFirst;
}
