//
// $Id: BuilderPanel.java,v 1.8 2004/08/27 02:12:26 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.cast.builder;

import javax.swing.*;
import com.samskivert.swing.*;

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
    public BuilderPanel (CharacterManager charmgr,
                         ComponentRepository crepo, String cprefix)
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
        sub.add(new ComponentPanel(model, cprefix));
        sub.add(new SpritePanel(charmgr, model));
        add(sub);
    }
}
