//
// $Id: TestApp.java,v 1.18 2004/08/27 02:20:54 mdb Exp $
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

package com.threerings.cast.builder;

import java.io.IOException;
import javax.swing.JFrame;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.image.ImageManager;
import com.threerings.resource.ResourceManager;

import com.threerings.cast.CharacterManager;
import com.threerings.cast.ComponentRepository;
import com.threerings.cast.bundle.BundledComponentRepository;

public class TestApp
{
    public TestApp (String[] args)
        throws IOException
    {
        _frame = new TestFrame();
        _frame.setSize(800, 600);
        SwingUtil.centerWindow(_frame);

        ResourceManager rmgr = new ResourceManager("rsrc");
        rmgr.initBundles(
            null, "config/resource/manager.properties", null, null);
        ImageManager imgr = new ImageManager(rmgr, _frame);

        ComponentRepository crepo =
            new BundledComponentRepository(rmgr, imgr, "components");
        CharacterManager charmgr = new CharacterManager(imgr, crepo);

        // initialize the frame
        ((TestFrame)_frame).init(charmgr, crepo);
    }

    public void run ()
    {
        _frame.pack();
        _frame.show();
    }

    public static void main (String[] args)
    {
        try {
            TestApp app = new TestApp(args);
            app.run();
        } catch (IOException ioe) {
            System.err.println("Error initializing app.");
            ioe.printStackTrace();
        }
    }

    protected JFrame _frame;
}
