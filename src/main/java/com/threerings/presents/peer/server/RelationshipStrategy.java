//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.presents.peer.server;

import java.util.List;

import com.threerings.presents.peer.server.persist.NodeRecord;
import com.threerings.presents.peer.server.persist.NodeRecord.Relationship;

/**
 * Used to determine the relationships between the local node and all others.
 */
public interface RelationshipStrategy
{
    /** A strategy in which all nodes are directly connected to one another. */
    public static final RelationshipStrategy FULLY_CONNECTED = new RelationshipStrategy() {
        public void determineRelationships (List<NodeRecord> nodes, String localNode) {
            for (NodeRecord node : nodes) {
                node.relationship = Relationship.DIRECT_EQUAL;
            }
        }
    };

    /** A strategy in which all child nodes connect to a single parent. */
    public static final RelationshipStrategy SINGLE_PARENT = new RelationshipStrategy() {
        public void determineRelationships (List<NodeRecord> nodes, String localNode) {
            String parentNode = localNode;
            for (NodeRecord node : nodes) {
                if (node.nodeName.compareTo(parentNode) < 0) {
                    parentNode = node.nodeName;
                }
            }
            boolean amParent = localNode.equals(parentNode);
            for (NodeRecord node : nodes) {
                node.relationship = amParent ? Relationship.CHILD :
                    (node.nodeName.equals(parentNode) ?
                        Relationship.PARENT : Relationship.INDIRECT);
            }
        }
    };

    /**
     * Determines the relationships between this node and all others.
     */
    public void determineRelationships (List<NodeRecord> nodes, String localNode);
}
