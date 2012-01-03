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

package com.threerings.presents.util;

import java.lang.reflect.Method;

import java.util.Map;

import com.samskivert.util.MethodFinder;

import static com.threerings.presents.Log.log;

/**
 * Class related utility functions.
 */
public class ClassUtil
{
    /**
     * Locates and returns the first method in the supplied class whose name is equal to the
     * specified name. If a method is located, it will be cached in the supplied cache so that
     * subsequent requests will immediately return the method from the cache rather than
     * re-reflecting.
     *
     * @return the method with the specified name or null if no method with that name could be
     * found.
     */
    public static Method getMethod (String name, Object target, Map<String, Method> cache)
    {
        Class<?> tclass = target.getClass();
        String key = tclass.getName() + ":" + name;
        Method method = cache.get(key);

        if (method == null) {
            method = findMethod(tclass, name);
            if (method != null) {
                cache.put(key, method);
            }
        }

        return method;
    }

    /**
     * Looks up the method on the specified object that has a signature that matches the supplied
     * arguments array. This is very expensive, so you shouldn't be doing this for something that
     * happens frequently.
     *
     * @return the best matching method with the specified name that accepts the supplied
     * arguments, or null if no method could be found.
     *
     * @see MethodFinder
     */
    public static Method getMethod (String name, Object target, Object[] args)
    {
        Class<?> tclass = target.getClass();
        Method meth = null;

        try {
            MethodFinder finder = new MethodFinder(tclass);
            meth = finder.findMethod(name, args);

        } catch (NoSuchMethodException nsme) {
            // nothing to do here but fall through and return null
            log.info("No such method", "name", name, "tclass", tclass.getName(), "args", args,
                     "error", nsme);

        } catch (SecurityException se) {
            log.warning("Unable to look up method?", "tclass", tclass.getName(), "mname", name);
        }

        return meth;
    }

    /**
     * Locates and returns the first method in the supplied class whose name is equal to the
     * specified name.
     *
     * @return the method with the specified name or null if no method with that name could be
     * found.
     */
    public static Method findMethod (Class<?> clazz, String name)
    {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if  (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }
}
