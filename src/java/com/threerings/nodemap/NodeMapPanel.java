//
// $Id: NodeMapPanel.java,v 1.7 2001/12/18 12:21:21 mdb Exp $

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

    /**
     * Returns the node map currently being displayed by this panel.
     */
    public NodeMap getNodeMap ()
    {
        return _map;
    }

    public void paintComponent (Graphics g)
    {
	super.paintComponent(g);
        if (_map != null) {
            // compute the offset necessary to center the map in the
            // display
            Dimension osize = getSize();
            Dimension msize = _map.getSize();
            int tx = (osize.width - msize.width)/2;
            int ty = (osize.height - msize.height)/2;
            g.translate(tx, ty);
            _map.paint(g);
            g.translate(-tx, -ty);
        }
    }

    public Dimension getPreferredSize ()
    {
        if (_map != null) {
            return _map.getSize();
        } else {
            return new Dimension(100, 100);
        }
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
        if (_map != null) {
            _map.handleMouseClicked(e.getX(), e.getY());
        }
    }

    public void mouseExited (MouseEvent e)
    {
        if (_map != null) {
            _map.handleMouseExited();
        }
    }

    public void mousePressed (MouseEvent e) { }
    public void mouseReleased (MouseEvent e) { }
    public void mouseEntered (MouseEvent e) { }

    /** MouseMotionListener interface methods */

    public void mouseMoved (MouseEvent e)
    {
        if (_map != null) {
            _map.handleMouseMoved(e.getX(), e.getY());
            repaint();
        }
    }

    public void mouseDragged (MouseEvent e) { }

    /** The node map. */
    protected NodeMap _map;
}
