//
// $Id: NodeMapPanel.java,v 1.8 2001/12/18 12:43:12 mdb Exp $

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

    /**
     * Instructs the panel to center the displayed node map on the
     * specified node. If the node is null, the panel will revert back to
     * centering on the entire map.
     */
    public void centerOnNode (Node node)
    {
        _centerNode = node;
        repaint();
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
	super.paintComponent(g);
        if (_map != null) {
            Dimension osize = getSize();
            int tx, ty;

            // compute the necessary centering offset
            if (_centerNode != null) {
                Point ncoords = _map.getNodeCoords(_centerNode);
                tx = (osize.width - _centerNode.getWidth())/2 - ncoords.x;
                ty = (osize.height - _centerNode.getHeight())/2 - ncoords.y;

            } else {
                Dimension msize = _map.getSize();
                tx = (osize.width - msize.width)/2;
                ty = (osize.height - msize.height)/2;
            }

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

    /** The node on which we're centering, or null. */
    protected Node _centerNode;
}
