//
// $Id: LayoutManager.java,v 1.1 2001/08/20 22:56:55 shaper Exp $

package com.threerings.nodemap;

import java.util.List;

/**
 * The layout manager class provides an interface that lays out nodes
 * in a node map via some preordained desirable methodology.
 */
public interface LayoutManager
{
    /**
     * Lay out the nodes in the list in the fashion dictated by this
     * particular layout manager.  The node positions are updated via
     * {@link Node#setPosition} such that subsequent rendering of the
     * nodes will place them in appropriate positions.
     *
     * @param root the root node for the graph.
     * @param nodes the list of nodes.
     */
    public void layout (Node root, List nodes);
}
