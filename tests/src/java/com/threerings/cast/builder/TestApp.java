//
// $Id: TestApp.java,v 1.12 2002/03/28 22:32:33 mdb Exp $

package com.threerings.cast.builder;

import java.io.IOException;
import javax.swing.JFrame;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.resource.ResourceManager;
import com.threerings.media.ImageManager;

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

        ResourceManager rmgr = new ResourceManager(
            "rsrc", null, "config/resource/manager.properties");
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

    /** The test frame. */
    protected JFrame _frame;
}
