//
// $Id: TestApp.java,v 1.5 2001/11/18 04:09:20 mdb Exp $

package com.threerings.cast.tools.builder;

import java.io.IOException;
import javax.swing.JFrame;

import com.samskivert.util.Config;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.resource.ResourceManager;

import com.threerings.cast.Log;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.ComponentRepository;

import com.threerings.miso.scene.MisoCharacterSprite;
import com.threerings.miso.util.MisoUtil;

public class TestApp
{
    public TestApp (String[] args)
    {
        _frame = new TestFrame();
        _frame.setSize(800, 600);
        SwingUtil.centerWindow(_frame);

        // create the handles on our various services
        _config = MisoUtil.createConfig();

        ResourceManager rsrcmgr = new ResourceManager("rsrc");

        // TBD: sort out component repository
        ComponentRepository crepo = null;
        CharacterManager charmgr = new CharacterManager(crepo);
        charmgr.setCharacterClass(MisoCharacterSprite.class);

        // initialize the frame
        ((TestFrame)_frame).init(charmgr);
    }

    public void run ()
    {
        _frame.pack();
        _frame.show();
    }

    public static void main (String[] args)
    {
        TestApp app = new TestApp(args);
        app.run();
    }

    /** The test frame. */
    protected JFrame _frame;

    /** The config object. */
    protected Config _config;
}
