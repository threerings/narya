//
// $Id: BuilderPanel.java,v 1.3 2001/11/02 01:10:28 shaper Exp $

package com.threerings.cast.builder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.*;

import com.samskivert.swing.*;
import com.samskivert.util.StringUtil;

import com.threerings.cast.*;

/**
 * The builder panel presents the user with an overview of a
 * composited character and facilities for altering the individual
 * components that comprise the character's display image.
 */
public class BuilderPanel extends JPanel
{
    /**
     * Constructs the builder panel.
     */
    public BuilderPanel (CharacterManager charmgr)
    {
        setLayout(new VGroupLayout());

	// give ourselves a wee bit of a border
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);

        // create the builder model
        BuilderModel model = new BuilderModel(charmgr);

        // create the component selection and sprite display panels
        JPanel sub = new JPanel(gl);
        sub.add(new ComponentPanel(model));
        sub.add(new SpritePanel(charmgr, model));
        add(sub);
    }
}
