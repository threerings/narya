//
// $Id: DumpBundle.java,v 1.4 2004/08/27 02:12:28 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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
                    bundle, BundleUtil.ACTIONS_PATH, false);
                dumpTable("actions: ", actions);

                HashMap actionSets = (HashMap)BundleUtil.loadObject(
                    bundle, BundleUtil.ACTION_SETS_PATH, false);
                dumpTable("actionSets: ", actionSets);

                HashMap classes = (HashMap)BundleUtil.loadObject(
                    bundle, BundleUtil.CLASSES_PATH, false);
                dumpTable("classes: ", classes);

                HashIntMap comps = (HashIntMap)BundleUtil.loadObject(
                    bundle, BundleUtil.COMPONENTS_PATH, false);
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
