//
// $Id: TestFrame.java,v 1.6 2002/06/19 23:24:19 mdb Exp $

package com.threerings.cast.builder;

import javax.swing.JFrame;

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

    public void init (CharacterManager charmgr, ComponentRepository crepo)
    {
        getContentPane().add(new BuilderPanel(charmgr, crepo, "male/"));
    }
}
