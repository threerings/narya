//
// $Id: TestApp.java,v 1.15 2002/07/19 20:13:29 shaper Exp $

package com.threerings.cast.builder;

import java.io.IOException;
import javax.swing.JFrame;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.ImageManager;
import com.threerings.resource.ResourceManager;

import com.threerings.cast.Log;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.ComponentRepository;
import com.threerings.cast.bundle.BundledComponentRepository;

import com.threerings.miso.scene.MisoCharacterSprite;

public class TestApp
{
    public TestApp (String[] args)
        throws IOException
    {
        _frame = new TestFrame();
        _frame.setSize(800, 600);
        SwingUtil.centerWindow(_frame);

        ResourceManager rmgr = new ResourceManager("rsrc");
        rmgr.initBundles(null, "config/resource/manager.properties", null);
        ImageManager imgr = new ImageManager(rmgr, _frame);

        ComponentRepository crepo =
            new BundledComponentRepository(rmgr, imgr, "components");
        CharacterManager charmgr = new CharacterManager(crepo);
        charmgr.setCharacterClass(MisoCharacterSprite.class);

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
