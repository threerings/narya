//
// $Id: DuplicateEdgeException.java,v 1.1 2001/08/20 22:56:55 shaper Exp $

package com.threerings.nodemap;

/**
 * Thrown when an action is attempted on an edge that is already
 * present in the involved node.
 */
public class DuplicateEdgeException extends NodeMapException
{
    public DuplicateEdgeException ()
    {
	super("error.duplicate_edge");
    }
}
