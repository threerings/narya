//
// $Id: SafeScrollPane.java,v 1.7 2003/03/22 01:56:09 mdb Exp $

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
