//
// $Id: CastUtil.java,v 1.2 2001/11/18 04:09:21 mdb Exp $

package com.threerings.cast.util;

import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.util.CollectionUtil;
import com.threerings.media.util.RandomUtil;

import com.threerings.cast.CharacterDescriptor;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.ComponentClass;

/**
 * Miscellaneous cast utility routines.
 */
public class CastUtil
{
    /**
     * Returns a new character descriptor populated with a random set of
     * components.
     */
    public static CharacterDescriptor getRandomDescriptor (
        CharacterManager charmgr)
    {
        // get all available classes
        ArrayList classes = new ArrayList();
        CollectionUtil.addAll(classes, charmgr.enumerateComponentClasses());

        // select the components
        int size = classes.size();
        int components[] = new int[size];
        for (int ii = 0; ii < size; ii++) {
            ComponentClass cclass = (ComponentClass)classes.get(ii);

            // get the components available for this class
            ArrayList choices = new ArrayList();
            Iterator iter = charmgr.enumerateComponentsByClass(cclass.clid);
            CollectionUtil.addAll(choices, iter);

            // choose a random component
            int idx = RandomUtil.getInt(choices.size());
            components[cclass.clid] = ((Integer)choices.get(idx)).intValue();
        }

        return new CharacterDescriptor(components);
    }
}
