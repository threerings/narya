//
// $Id: TestApp.java,v 1.8 2001/11/29 22:01:59 mdb Exp $

package com.threerings.cast.builder;

import java.io.IOException;
import javax.swing.JFrame;

import com.samskivert.util.Config;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.resource.ResourceManager;

import com.threerings.cast.Log;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.ComponentRepository;
import com.threerings.cast.bundle.BundledComponentRepository;

import com.threerings.miso.scene.MisoCharacterSprite;
import com.threerings.miso.util.MisoUtil;

public class TestApp
{
    public TestApp (String[] args)
        throws IOException
    {
        _frame = new TestFrame();
        _frame.setSize(800, 600);
        SwingUtil.centerWindow(_frame);

        // create the handles on our various services
        _config = new Config();
        MisoUtil.bindProperties(_config);

        ResourceManager rmgr = new ResourceManager(null, "rsrc");
        ComponentRepository crepo =
            new BundledComponentRepository(rmgr, "components");
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

    /** The config object. */
    protected Config _config;
}
