//
// $Id: BundledComponentRepositoryTest.java,v 1.1 2001/11/27 08:06:57 mdb Exp $

package com.threerings.cast.bundle;

import java.util.Iterator;
import com.samskivert.util.StringUtil;
import com.threerings.resource.ResourceManager;
import com.threerings.cast.ComponentClass;

public class BundledComponentRepositoryTest
{
    public static void main (String[] args)
    {
        try {
            ResourceManager rmgr = new ResourceManager(null, "rsrc");
            BundledComponentRepository repo =
                new BundledComponentRepository(rmgr, "components");

            System.out.println("Classes: " + StringUtil.toString(
                                   repo.enumerateComponentClasses()));

            System.out.println("Actions: " + StringUtil.toString(
                                   repo.enumerateActionSequences()));

            System.out.println("Action sets: " + StringUtil.toString(
                                   repo._actionSets.values().iterator()));

            Iterator iter = repo.enumerateComponentClasses();
            while (iter.hasNext()) {
                ComponentClass cclass = (ComponentClass)iter.next();
                System.out.println("IDs [" + cclass + "]: " +
                                   StringUtil.toString(
                                       repo.enumerateComponentIds(cclass)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
