//
// $Id: SafeScrollPane.java,v 1.1 2002/04/23 01:16:27 mdb Exp $

package com.threerings.media;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * A scroll pane that is safe to use in frame managed views.
 */
public class SafeScrollPane extends JScrollPane
{
    public SafeScrollPane (Component view)
    {
        super(view);
    }

    protected JViewport createViewport ()
    {
        JViewport vp = new JViewport();
        vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        return vp;
    }
}
