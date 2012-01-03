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

import java.lang.reflect.Type;

import java.util.List;

import com.google.common.collect.Lists;

import com.threerings.presents.tools.InvocationTask.ServiceMethod;

public class CPPArgBuilder
{
    public String getArguments (ServiceMethod meth)
    {
        return getArguments(meth, "");
    }

    public String getArguments (ServiceMethod meth, String prefix)
    {
        StringBuilder buf = new StringBuilder(prefix);
        Type[] ptypes = meth.method.getGenericParameterTypes();
        for (int ii = 0; ii < ptypes.length; ii++) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(CPPUtil.getCPPType(ptypes[ii])).append(" arg").append(ii+1);
        }
        return buf.toString();
    }

    public List<String> getArgumentNames (ServiceMethod meth)
    {
        Type[] ptypes = meth.method.getGenericParameterTypes();
        List<String> args = Lists.newArrayListWithCapacity(ptypes.length);
        for (int ii = 0; ii < ptypes.length; ii++) {
            args.add("arg" + (ii+1));
        }
        return args;
    }

    public String getArgumentsFromVector (ServiceMethod meth)
    {
        StringBuilder buf = new StringBuilder();
        Type[] ptypes = meth.method.getGenericParameterTypes();
        for (int ii = 0; ii < ptypes.length; ii++) {
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
        for (int ii = 0; ii < ptypes.length; ii++) {
            args.add(new CPPType(ptypes[ii]).getAsStreamable("arg" + (ii+1)));
        }
        return args;
    }
}
