//
// $Id: ClassUtil.java,v 1.2 2001/08/14 06:48:08 mdb Exp $

package com.threerings.cocktail.cher.util;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.samskivert.Log;
import com.samskivert.util.StringUtil;

/**
 * Class related utility functions.
 */
public class ClassUtil
{
    /**
     * Locates and returns the first method in the supplied class whose
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
     * Looks up the method on the specified object that has a signature
     * that matches the supplied arguments array. This has to create an
     * array of class objects correspnding to the types of all of the
     * arguments in the args array, so none of them are allowed to be
     * null. This is very expensive, so you shouldn't be doing this for
     * something that happens frequently.
     *
     * <p><em>Note:</em> primitive types for arguments are preferred. What
     * this means is that if an element of the <code>args</code> array is
     * an instance of <code>Integer</code>, for example, then this code
     * assumes that it is intended for that to be unwrapped into an
     * <code>int</code> in calling the actual function. The reason this is
     * necessary is that method lookup isn't smart about first checking
     * for a method taking the primitive argument and then falling back to
     * one taking an <code>Integer</code> object. So we insist that all
     * arguments that <em>can</em> be unwrapped, <em>are</em> unwrapped.
     * Why would you want to pass <code>Integer</code> objects as
     * arguments to your function anyway?
     *
     * @return the best matching method with the specified name that
     * accepts the supplied arguments, or null if no method could be
     * found.
     */
    public static Method getMethod (String name, Object target, Object[] args)
    {
        // grab a whole crapload of class objects
        Class tclass = target.getClass();
        Class[] aclasses = new Class[args.length];
        for (int i = 0; i < aclasses.length; i++) {
            Class aclass = args[i].getClass();
            Class mclass = (Class)_classMap.get(aclass);
            // use the massaged class if there is one
            aclasses[i] = (mclass == null) ? aclass : mclass;
        }

        // now look up the method
        Method meth = null;
        try {
            meth = tclass.getMethod(name, aclasses);
        } catch (NoSuchMethodException nsme) {
            // nothing to do here but fall through and return null
            Log.info("No such method [name=" + name +
                     ", tclass=" + tclass.getName() + 
                     ", args=" + StringUtil.toString(aclasses) + "].");

        } catch (SecurityException se) {
            Log.warning("Unable to look up method? " +
                        "[tclass=" + tclass.getName() +
                        ", mname=" + name + "].");
        }
        return meth;
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

    /** Used when massaging arguments into their primitive type. */
    protected static HashMap _classMap = new HashMap();
    static {
        _classMap.put(Boolean.class, Boolean.TYPE);
        _classMap.put(Byte.class, Byte.TYPE);
        _classMap.put(Character.class, Character.TYPE);
        _classMap.put(Short.class, Short.TYPE);
        _classMap.put(Integer.class, Integer.TYPE);
        _classMap.put(Long.class, Long.TYPE);
        _classMap.put(Float.class, Float.TYPE);
        _classMap.put(Double.class, Double.TYPE);
    }
}
