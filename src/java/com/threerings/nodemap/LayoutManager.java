//
// $Id: LayoutManager.java,v 1.3 2004/08/27 02:20:11 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
