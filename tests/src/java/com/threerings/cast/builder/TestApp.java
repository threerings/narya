//
// $Id: TestApp.java,v 1.3 2001/11/08 02:07:36 mdb Exp $

package com.threerings.cast.builder;

import java.io.IOException;
import javax.swing.JFrame;

import com.samskivert.util.Config;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.ImageManager;

import com.threerings.cast.Log;
import com.threerings.cast.CharacterManager;

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
	_imgmgr = MisoUtil.createImageManager(_frame);

        CharacterManager charmgr =
            MisoUtil.createCharacterManager(_config, _imgmgr);

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

    /** The image manager object. */
    protected ImageManager _imgmgr;
}
