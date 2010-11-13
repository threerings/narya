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
        vectorArguments = new CPPArgBuilder(false).getArgumentsFromVector(methodSource);
        arguments = new CPPArgBuilder(false).getArguments(methodSource);
        clientArguments = new CPPArgBuilder(true).getArguments(methodSource, "Shared<presents::PresentsClient> client");
        serviceArguments = new CPPArgBuilder(true).getServiceArguments(methodSource);
    }
}