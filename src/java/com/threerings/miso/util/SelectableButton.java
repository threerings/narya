//
// $Id: SelectableButton.java,v 1.2 2004/08/27 02:20:10 mdb Exp $
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

package com.threerings.miso.util;

import java.awt.*;
import javax.swing.*;

/**
 * A selectable button is just a normal <code>JButton</code> that
 * paints itself with a different background color when selected.
 * This is frequently desirable; for instance, buttons may be included
 * in a toolbar and shown highlighted as long as their associated
 * "mode" is active.
 *
 * <p> <code>JButton</code> does provide support for rendering itself
 * differently via <code>setSelectedIcon()</code>, but creating
 * selected and unselected icons for each button simply to effect an
 * alteration in appearance while selected is sometimes more than a
 * mere programmer may wish to deal with.
 *
 * <p> The <code>JRadioButton</code> might be usable excepting that it
 * doesn't look as visually appealing as the <code>JButton</code> when
 * painted (it lacks nice borders and shading, and manually adding
 * those to the radio button is even more of a hack than this, and
 * still doesn't look quite right.)
 */
public class SelectableButton extends JButton
{
    public SelectableButton ()
    {
	init();
    }

    public SelectableButton (Icon icon)
    {
	super(icon);
	init();
    }

    public SelectableButton (String text)
    {
	super(text);
	init();
    }

    public SelectableButton (Action a)
    {
	super(a);
	init();
    }

    public SelectableButton (String text, Icon icon)
    {
	super(text, icon);
	init();
    }

    protected void init ()
    {
	setOpaque(false);
    }

    public void paintComponent (Graphics g)
    {
  	if (isSelected()) {
	    g.setColor(_scol);
	    Dimension size = getSize();
  	    g.fillRect(0, 0, size.width, size.height);
  	}

	super.paintComponent(g);
    }

    /**
     * Set the color used to paint the button background when selected.
     *
     * @param scol the selected color.
     */
    public void setSelectedColor (Color scol)
    {
	_scol = scol;
    }

    /** The default selected color. */
    protected static final Color DEF_SELECT_COLOR = new Color(152, 152, 152);

    /** The selected color. */
    protected Color _scol = DEF_SELECT_COLOR;
}
