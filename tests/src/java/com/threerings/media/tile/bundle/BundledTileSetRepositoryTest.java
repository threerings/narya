//
// $Id: BundledTileSetRepositoryTest.java,v 1.10 2004/08/27 02:20:58 mdb Exp $
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

package com.threerings.media.tile.bundle;

import java.util.Iterator;

import com.threerings.media.image.ImageManager;
import com.threerings.resource.ResourceManager;

import junit.framework.Test;
import junit.framework.TestCase;

public class BundledTileSetRepositoryTest extends TestCase
{
    public BundledTileSetRepositoryTest ()
    {
        super(BundledTileSetRepositoryTest.class.getName());
    }

    public void runTest ()
    {
        try {
            ResourceManager rmgr = new ResourceManager("rsrc");
            rmgr.initBundles(
                null, "config/resource/manager.properties", null, null);
            BundledTileSetRepository repo = new BundledTileSetRepository(
                rmgr, new ImageManager(rmgr, null), "tilesets");
            Iterator sets = repo.enumerateTileSets();
            while (sets.hasNext()) {
                sets.next();
//                 System.out.println(sets.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite ()
    {
        return new BundledTileSetRepositoryTest();
    }

    public static void main (String[] args)
    {
        BundledTileSetRepositoryTest test =
            new BundledTileSetRepositoryTest();
        test.runTest();
    }
}
