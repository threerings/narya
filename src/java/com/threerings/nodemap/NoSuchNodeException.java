//
// $Id: NoSuchNodeException.java,v 1.1 2001/08/20 22:56:55 shaper Exp $

package com.threerings.nodemap;

/**
 * Thrown when a node cannot be found in the node map.
 */
public class NoSuchNodeException extends NodeMapException
{
    public NoSuchNodeException ()
    {
	super("error.no_such_node");
    }
}
