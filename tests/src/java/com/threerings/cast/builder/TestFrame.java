//
// $Id: TestFrame.java,v 1.3 2001/11/18 04:09:20 mdb Exp $

package com.threerings.cast.tools.builder;

import javax.swing.JFrame;

import com.threerings.cast.CharacterManager;

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
