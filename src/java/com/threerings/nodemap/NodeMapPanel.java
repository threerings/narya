//
// $Id: NodeMapPanel.java,v 1.3 2001/08/28 23:50:45 shaper Exp $

package com.threerings.nodemap;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.samskivert.swing.ToolTipManager;
import com.samskivert.swing.ToolTipObserver;
import com.samskivert.swing.ToolTipProvider;

/**
 * The node map panel handles display of a node map and passes Swing
 * UI events on to the appropriate node map event handling methods.
 */
public class NodeMapPanel extends JPanel
    implements MouseListener, MouseMotionListener, ToolTipObserver
{
    public NodeMapPanel (NodeMap map)
    {
	_map = map;

	// create the tool tip manager and inform the node map
	_map.setToolTipManager(new ToolTipManager(this));

	// listen to our mouse events
	addMouseListener(this);
	addMouseMotionListener(this);
    }

    public void paintComponent (Graphics g)
    {
	super.paintComponent(g);
	_map.paint(g);
    }

    public Dimension getPreferredSize ()
    {
	return _map.getSize();
    }

    public void showToolTip (ToolTipProvider tipper, int x, int y)
    {
	_map.setToolTipProvider(tipper, x, y);
	repaint();
    }

    public void hideToolTip ()
    {
	showToolTip(null, -1, -1);
    }

    public JComponent getComponent ()
    {
	return this;
    }

    /** MouseListener interface methods */

    public void mouseClicked (MouseEvent e)
    {
	_map.handleMouseClicked(e.getX(), e.getY());
    }

    public void mouseExited (MouseEvent e)
    {
	_map.handleMouseExited();
    }

    public void mousePressed (MouseEvent e) { }
    public void mouseReleased (MouseEvent e) { }
    public void mouseEntered (MouseEvent e) { }

    /** MouseMotionListener interface methods */

    public void mouseMoved (MouseEvent e)
    {
	_map.handleMouseMoved(e.getX(), e.getY());
	repaint();
    }

    public void mouseDragged (MouseEvent e) { }

    /** The node map. */
    protected NodeMap _map;
}
