//
// $Id: TestFrame.java,v 1.2 2001/11/08 02:07:36 mdb Exp $

package com.threerings.cast.builder;

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
