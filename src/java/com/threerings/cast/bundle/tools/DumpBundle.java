//
// $Id: DumpBundle.java,v 1.2 2002/02/05 20:29:09 mdb Exp $

package com.threerings.cast.bundle.tools;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.resource.ResourceBundle;
import com.threerings.cast.bundle.BundleUtil;

/**
 * Dumps the contents of a component bundle to stdout.
 */
public class DumpBundle
{
    public static void main (String[] args)
    {
        if (args.length < 1) {
            String usage = "Usage: DumpBundle bundle.jar [bundle.jar ...]";
            System.err.println(usage);
            System.exit(-1);
        }

        for (int i = 0; i < args.length; i++) {
            File file = new File(args[i]);
            try {
                ResourceBundle bundle = new ResourceBundle(file);

                HashMap actions = (HashMap)BundleUtil.loadObject(
                    bundle, BundleUtil.ACTIONS_PATH);
                dumpTable("actions: ", actions);

                HashMap actionSets = (HashMap)BundleUtil.loadObject(
                    bundle, BundleUtil.ACTION_SETS_PATH);
                dumpTable("actionSets: ", actionSets);

                HashMap classes = (HashMap)BundleUtil.loadObject(
                    bundle, BundleUtil.CLASSES_PATH);
                dumpTable("classes: ", classes);

                HashIntMap comps = (HashIntMap)BundleUtil.loadObject(
                    bundle, BundleUtil.COMPONENTS_PATH);
                dumpTable("components: ", comps);

            } catch (Exception e) {
                System.err.println("Error dumping bundle [path=" + args[i] +
                                   ", error=" + e + "].");
                e.printStackTrace();
            }
        }
    }

    protected static void dumpTable (String prefix, Map table)
    {
        if (table != null) {
            Iterator iter = table.entrySet().iterator();
            System.out.println(prefix + StringUtil.toString(iter));
        }
    }        
}
