//
// $Id: CastUtil.java,v 1.7 2002/03/27 20:31:11 mdb Exp $

package com.threerings.cast.util;

import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.util.CollectionUtil;
import com.samskivert.util.StringUtil;
import com.threerings.media.util.RandomUtil;

import com.threerings.cast.CharacterDescriptor;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.ComponentClass;
import com.threerings.cast.ComponentRepository;
import com.threerings.cast.Log;

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
        String gender, ComponentRepository crepo)
    {
        // get all available classes
        ArrayList classes = new ArrayList();
        for (int i = 0; i < CLASSES.length; i++) {
            String cname = gender + "/" + CLASSES[i];
            ComponentClass cclass = crepo.getComponentClass(cname);
            if (cclass == null) {
                Log.warning("Missing definition for component class " +
                            "[class=" + cname + "].");
            } else {
                classes.add(cclass);
            }
        }

        // select the components
        int size = classes.size();
        int components[] = new int[size];
        for (int ii = 0; ii < size; ii++) {
            ComponentClass cclass = (ComponentClass)classes.get(ii);

            // get the components available for this class
            ArrayList choices = new ArrayList();
            Iterator iter = crepo.enumerateComponentIds(cclass);
            CollectionUtil.addAll(choices, iter);

            // choose a random component
            int idx = RandomUtil.getInt(choices.size());
            components[ii] = ((Integer)choices.get(idx)).intValue();
        }

        return new CharacterDescriptor(components, null);
    }

    protected static final String[] CLASSES = {
        "legs", "feet", "hand_left", "hand_right", "torso",
        "head", "hair", "hat", "eyepatch" };
}
