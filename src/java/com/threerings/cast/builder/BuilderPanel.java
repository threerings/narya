//
// $Id: BuilderPanel.java,v 1.6 2002/04/23 01:17:28 mdb Exp $

package com.threerings.cast.builder;

import javax.swing.*;
import com.samskivert.swing.*;

import com.threerings.media.FrameManager;

import com.threerings.cast.CharacterManager;
import com.threerings.cast.ComponentRepository;

/**
 * The builder panel presents the user with an overview of a composited
 * character and facilities for altering the individual components that
 * comprise the character's display image.
 */
public class BuilderPanel extends JPanel
{
    /**
     * Constructs the builder panel.
     */
    public BuilderPanel (FrameManager framemgr, CharacterManager charmgr,
                         ComponentRepository crepo)
    {
        setLayout(new VGroupLayout());

	// give ourselves a wee bit of a border
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);

        // create the builder model
        BuilderModel model = new BuilderModel(crepo);

        // create the component selection and sprite display panels
        JPanel sub = new JPanel(gl);
        sub.add(new ComponentPanel(model));
        sub.add(new SpritePanel(framemgr, charmgr, model));
        add(sub);
    }
}
