//
// $Id: BundledComponentRepositoryTest.java,v 1.4 2001/12/07 01:33:29 mdb Exp $

package com.threerings.cast.bundle;

import java.util.Iterator;
import com.samskivert.util.StringUtil;
import com.threerings.resource.ResourceManager;
import com.threerings.media.ImageManager;
import com.threerings.cast.ComponentClass;

import junit.framework.Test;
import junit.framework.TestCase;

public class BundledComponentRepositoryTest extends TestCase
{
    public BundledComponentRepositoryTest ()
    {
        super(BundledComponentRepositoryTest.class.getName());
    }

    public void runTest ()
    {
        try {
            ResourceManager rmgr = new ResourceManager(null, "rsrc");
            ImageManager imgr = new ImageManager(rmgr, null);
            BundledComponentRepository repo =
                new BundledComponentRepository(rmgr, imgr, "components");

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
            fail();
        }
    }

    public static void main (String[] args)
    {
        BundledComponentRepositoryTest test =
            new BundledComponentRepositoryTest();
        test.runTest();
    }

    public static Test suite ()
    {
        return new BundledComponentRepositoryTest();
    }
}
