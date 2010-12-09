package com.threerings.presents.tools.cpp;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.Collections;
import java.util.List;
import java.io.File;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.threerings.presents.dobj.DSet;

public class CPPUtil
{
    public static String getCPPType (Type ftype)
    {
        if (ftype.equals(com.threerings.presents.dobj.DSet.Entry.class)) {
            return "Shared<Streamable>";
        }
        if (ftype instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType)ftype).getActualTypeArguments();
            Type raw = ((ParameterizedType)ftype).getRawType();
            if (raw.equals(Comparable.class)) {
                return "Shared<Streamable>";
            } else if (raw.equals(List.class)) {
                return "Shared< std::vector< " + getCPPType(typeArguments[0]) + " > >";
            } else if (raw.equals(DSet.class)) {
                ftype = raw;
            } else {
                throw new IllegalArgumentException("Don't know how to handle " + raw);
            }
        }

        if (ftype.equals(Boolean.class) || ftype.equals(Byte.class) || ftype.equals(Short.class)
            || ftype.equals(Integer.class) || ftype.equals(Long.class)
            || ftype.equals(Float.class) || ftype.equals(Double.class)) {
            throw new IllegalArgumentException("Presents can't handle boxed types in C++");
        }
        if (ftype.equals(String.class)) {
            return "Shared<utf8>";
        } else if (ftype.equals(Boolean.TYPE)) {
            return "bool";
        } else if (ftype.equals(Byte.TYPE)) {
            return "int8";
        } else if (ftype.equals(Short.TYPE)) {
            return "int16";
        } else if (ftype.equals(Integer.TYPE)) {
            return "int32";
        } else if (ftype.equals(Long.TYPE)) {
            return "int64";
        } else if (ftype.equals(Float.TYPE)) {
            return "float";
        } else if (ftype.equals(Double.TYPE)) {
            return "double";
        } else if (ftype.equals(Object.class) || ftype instanceof TypeVariable<?>) {
            return "Shared<Streamable>";
        } else {
            return "Shared<" + makeCPPName((Class<?>)ftype) + ">";
        }
    }

    public static String makeCPPName (Class<?> sclass)
    {
        return makeCPPName(makeNamespaces(sclass), sclass.getSimpleName());
    }

    public static String makeCPPName (List<String> namespaces, String className)
    {
        return Joiner.on("::").join(namespaces) + "::" + className;
    }

    public static String makePath (Class<?> klass, String ext)
    {
        return makePath(makeNamespaces(klass), klass.getSimpleName(), ext);
    }

    public static String makePath (List<String> namespaces, String className, String ext)
    {
        return Joiner.on(File.separator).join(namespaces) + File.separator + className+ ext;
    }

    public static String makePath (File root, Class<?> klass, String ext)
    {
        return new File(root, makePath(klass, ext)).getAbsolutePath();
    }


    public static String makePath (File root, List<String> namespaces, String className, String ext)
    {
        return new File(root, makePath(namespaces, className, ext)).getAbsolutePath();
    }

    public static List<String> makeNamespaces (String pack)
    {
        Iterable<String> split = Splitter.on(".").split(pack);
        List<String> segs = Lists.newArrayList(split);
        if (segs.size() > 1 && segs.get(0).equals("com") && segs.get(1).equals("threerings")) {
            segs.remove(0);
            segs.remove(0);
        }
        return segs;

    }

    public static List<String> makeNamespaces (Class<?> sclass)
    {
        if (sclass.getPackage() == null) {
            return Collections.emptyList();
        } else {
            return makeNamespaces(sclass.getPackage().getName());
        }
    }

    public static String makeNamespace (Class<?> sclass)
    {
        return Joiner.on("::").join(makeNamespaces(sclass));
    }
}
