//
// $Id: DEventUtil.java,v 1.2 2001/06/01 20:35:39 mdb Exp $

package com.threerings.cocktail.cher.dobj;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * A repository for distributed object event related utility functions.
 */
public class DEventUtil
{
    /**
     * Looks up and returns the method used to set an attribute of the
     * specified name on an instance of the specified distributed object
     * class.
     */
    public static Method getSetter (Class clazz, String name)
        throws ObjectAccessException
    {
        // first see if it's cached
        Method setter = (Method)_setterCache.get(name);
        if (setter != null) {
            return setter;
        }

        // convert the attribute name into a setter name
        String sname = setterName(name);
        try {
            // look up that method (we don't have the parameter types, so
            // we have to do our own name matching)
            Method[] methods = clazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(sname)) {
                    setter = methods[i];
                    break;
                }
            }

        } catch (SecurityException se) {
            throw new ObjectAccessException("Reflection error: " + se);
        }

        // make sure we found a matching method
        if (setter == null) {
            String errmsg = "No setter for attribute '" + name + "'.";
            throw new ObjectAccessException(errmsg);
        }

        // and cache it
        _setterCache.put(name, setter);
        return setter;
    }

    protected static String setterName (String name)
    {
        StringBuffer sname = new StringBuffer();
        sname.append("set");
        sname.append(Character.toUpperCase(name.charAt(0)));
        sname.append(name.substring(1));
        return sname.toString();
    }

    protected static HashMap _setterCache = new HashMap();
}
