//
// $Id: ClassUtil.java,v 1.1 2001/07/19 07:09:16 mdb Exp $

package com.threerings.cocktail.cher.util;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Class related utility functions.
 */
public class ClassUtil
{
    /**
     * Locates and returns teh first method in the supplied class whose
     * name is equal to the specified name. If a method is located, it
     * will be cached in the supplied cache so that subsequent requests
     * will immediately return the method from the cache rather than
     * re-reflecting.
     *
     * @return the method with the specified name or null if no method
     * with that name could be found.
     */
    public static Method getMethod (String name, Object target, HashMap cache)
    {
        Class tclass = target.getClass();
        String key = tclass.getName() + ":" + name;
        Method method = (Method)cache.get(key);

        if (method == null) {
            method = findMethod(tclass, name);
            if (method != null) {
                cache.put(key, method);
            }
        }

        return method;
    }

    /**
     * Locates and returns the first method in the supplied class whose
     * name is equal to the specified name.
     *
     * @return the method with the specified name or null if no method
     * with that name could be found.
     */
    public static Method findMethod (Class clazz, String name)
    {
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if  (methods[i].getName().equals(name)) {
                return methods[i];
            }
        }
        return null;
    }
}
