//
// $Id: TestApp.java,v 1.1 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast.builder.test;

import java.io.IOException;
import javax.swing.JFrame;

import com.samskivert.util.Config;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.cast.Log;
import com.threerings.cast.CharacterManager;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.util.MisoContext;
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
	_tilemgr = MisoUtil.createTileManager(_config, _frame);

        CharacterManager charmgr =
            MisoUtil.createCharacterManager(_config, _tilemgr);

        // create the context object
        _ctx = new TestContextImpl();

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

    protected class TestContextImpl implements MisoContext
    {
	public Config getConfig ()
	{
	    return _config;
	}

	public TileManager getTileManager ()
	{
	    return _tilemgr;
	}
    }

    /** The test frame. */
    protected JFrame _frame;

    /** The test context. */
    protected MisoContext _ctx;

    /** The config object. */
    protected Config _config;

    /** The tile manager object. */
    protected TileManager _tilemgr;
}
