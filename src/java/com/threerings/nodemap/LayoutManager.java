//
// $Id: LayoutManager.java,v 1.2 2001/09/13 19:08:30 mdb Exp $

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
     * {@link Node#setLocation} such that subsequent rendering of the
     * nodes will place them in appropriate positions.
     *
     * @param root the root node for the graph.
     * @param nodes the list of nodes.
     */
    public void layout (Node root, List nodes);
}
