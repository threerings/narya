//
// $Id: NodeMapPanel.java,v 1.5 2001/10/11 00:41:27 shaper Exp $

package com.threerings.nodemap;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.samskivert.swing.ToolTipManager;
import com.samskivert.swing.ToolTipObserver;
import com.samskivert.swing.ToolTipProvider;

/**
 * The node map panel handles display of a node map and passes user
 * interface events on to the appropriate node map event handling
 * methods.
 */
public class NodeMapPanel extends JPanel
    implements MouseListener, MouseMotionListener, ToolTipObserver
{
    /**
     * Constructs a node map panel.
     */
    public NodeMapPanel ()
    {
	init();
    }

    /**
     * Constructs a node map panel that displays the given node map.
     *
     * @param map the node map to display.
     */
    public NodeMapPanel (NodeMap map)
    {
	init();
	setNodeMap(map);
    }

    /**
     * Initializes the node map panel.
     */
    protected void init ()
    {
	// listen to our mouse events
	addMouseListener(this);
	addMouseMotionListener(this);
    }

    /**
     * Sets the node map displayed by this panel.
     *
     * @param map the node map to display.
     */
    public void setNodeMap (NodeMap map)
    {
	_map = map;

	// create the tool tip manager and inform the node map
	_map.setToolTipManager(new ToolTipManager(this));
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
