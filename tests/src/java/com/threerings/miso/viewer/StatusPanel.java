//
// $Id: StatusPanel.java,v 1.1 2001/08/07 18:29:18 shaper Exp $

package com.threerings.miso.viewer;

import java.awt.*;
import javax.swing.*;

import com.threerings.miso.Log;

public class StatusPanel extends JPanel
{
    public void paintComponent (Graphics g)
    {
	super.paintComponent(g);

	g.setColor(Color.yellow);
	Rectangle bounds = getBounds();
	g.fillRect(0, 0, bounds.width, bounds.height);

    	Log.info("status panel [bounds=" + bounds + "].");
    }

    public Dimension getPreferredSize ()
    {
	return new Dimension(200, 150);
    }
}
