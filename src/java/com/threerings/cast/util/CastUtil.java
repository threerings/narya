//
// $Id: CastUtil.java,v 1.8 2002/03/27 21:48:41 mdb Exp $

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

            // make sure the component class exists
            if (cclass == null) {
                Log.warning("Missing definition for component class " +
                            "[class=" + cname + "].");
                continue;
            }

            // make sure there are some components in this class
            Iterator iter = crepo.enumerateComponentIds(cclass);
            if (!iter.hasNext()) {
                Log.info("Skipping class for which we have no components " +
                         "[class=" + cclass + "].");
                continue;
            }

            classes.add(cclass);
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
            if (choices.size() > 0) {
                int idx = RandomUtil.getInt(choices.size());
                components[ii] = ((Integer)choices.get(idx)).intValue();
            } else {
                Log.info("Have no components in class [class=" + cclass + "].");
            }
        }

        return new CharacterDescriptor(components, null);
    }

    protected static final String[] CLASSES = {
        "legs", "feet", "hand_left", "hand_right", "torso",
        "head", "hair", "hat", "eyepatch" };
}
