//
// $Id: TestFrame.java,v 1.1 2001/10/30 16:16:01 shaper Exp $

package com.threerings.cast.builder.test;

import javax.swing.JFrame;

import com.threerings.cast.CharacterManager;
import com.threerings.cast.builder.BuilderPanel;

public class TestFrame extends JFrame
{
    public TestFrame ()
    {
	super("Character Builder");

        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void init (CharacterManager charmgr)
    {
        getContentPane().add(new BuilderPanel(charmgr));
    }
}
