//
// $Id: SafeScrollPane.java,v 1.8 2004/08/27 02:12:37 mdb Exp $
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

package com.threerings.media;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * A scroll pane that is safe to use in frame managed views.
 */
public class SafeScrollPane extends JScrollPane
{
    public SafeScrollPane ()
    {
    }

    public SafeScrollPane (Component view)
    {
        super(view);
    }

    public SafeScrollPane (Component view, int owidth, int oheight)
    {
        super(view);
        if (owidth != 0 || oheight != 0) {
            _override = new Dimension(owidth, oheight);
        }
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        Dimension d = super.getPreferredSize();
        if (_override != null) {
            if (_override.width != 0) {
                d.width = _override.width;
            }
            if (_override.height != 0) {
                d.height = _override.height;
            }
        }
        return d;
    }

    protected JViewport createViewport ()
    {
        JViewport vp = new JViewport() {
            public void setViewPosition (Point p) {
                super.setViewPosition(p);
                // simple scroll mode results in setViewPosition causing
                // our view to become invalid, but nothing ever happens to
                // queue up a revalidate for said view, so we have to do
                // it here
                Component c = getView();
                if (c instanceof JComponent) {
                    ((JComponent)c).revalidate();
                }
            }
        };
        vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        return vp;
    }

    protected Dimension _override;
}
