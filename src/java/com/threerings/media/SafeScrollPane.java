//
// $Id: SafeScrollPane.java,v 1.5 2002/11/05 05:51:18 mdb Exp $

package com.threerings.media;

import java.awt.Component;
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
                    System.out.println("Revalidating " + c);
                    ((JComponent)c).revalidate();
                }
            }
        };
        vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        return vp;
    }
}
