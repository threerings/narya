//
// $Id: DuplicateNodeException.java,v 1.1 2001/08/20 22:56:55 shaper Exp $

package com.threerings.nodemap;

/**
 * Thrown when an action is attempted on a node that is already
 * present in the node map.
 */
public class DuplicateNodeException extends NodeMapException
{
    public DuplicateNodeException ()
    {
	super("error.duplicate_node");
    }
}
