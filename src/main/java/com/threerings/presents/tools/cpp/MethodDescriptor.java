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

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import com.threerings.presents.tools.InvocationTask.ServiceMethod;

public class MethodDescriptor
{
    public static List<MethodDescriptor> from(List<ServiceMethod> methods) {
        return Lists.transform(methods, new Function<ServiceMethod, MethodDescriptor>() {
            public MethodDescriptor apply (ServiceMethod from) {
                return new MethodDescriptor(from);
            }});
    }

    public final String methodName;
    public final String vectorArguments;
    public final String arguments;
    public final String clientArguments;
    public final List<String> serviceArguments;

    public MethodDescriptor(ServiceMethod methodSource) {
        methodName = methodSource.method.getName();
        vectorArguments = new CPPArgBuilder().getArgumentsFromVector(methodSource);
        arguments = new CPPArgBuilder().getArguments(methodSource);
        clientArguments = new CPPArgBuilder().getArguments(
            methodSource, "Shared<presents::PresentsClient> client");
        serviceArguments = new CPPArgBuilder().getServiceArguments(methodSource);
    }
}
