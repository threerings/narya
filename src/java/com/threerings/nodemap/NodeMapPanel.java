//
// $Id: NodeMapPanel.java,v 1.2 2001/08/23 00:22:30 shaper Exp $

package com.threerings.nodemap;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.samskivert.swing.ToolTipManager;
import com.samskivert.swing.ToolTipObserver;

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

    public void showToolTip (Object target)
    {
	_map.setToolTipNode((Node)target);
	repaint();
    }

    public void hideToolTip ()
    {
	showToolTip(null);
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

    public void mousePressed (MouseEvent e) { }
    public void mouseReleased (MouseEvent e) { }
    public void mouseEntered (MouseEvent e) { }
    public void mouseExited (MouseEvent e) { }

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
