//
// $Id: TestFrame.java,v 1.5 2002/04/23 01:19:04 mdb Exp $

package com.threerings.cast.builder;

import javax.swing.JFrame;

import com.threerings.media.FrameManager;

import com.threerings.cast.CharacterManager;
import com.threerings.cast.ComponentRepository;

public class TestFrame extends JFrame
{
    public TestFrame ()
    {
	super("Character Builder");

        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void init (FrameManager framemgr, CharacterManager charmgr,
                      ComponentRepository crepo)
    {
        getContentPane().add(new BuilderPanel(framemgr, charmgr, crepo));
    }
}
