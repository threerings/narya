//
// $Id: NodeMapException.java,v 1.1 2001/08/20 22:56:55 shaper Exp $

package com.threerings.nodemap;

/**
 * Thrown when an error occurs while performing certain activities
 * involving a node map object.
 */
public class NodeMapException extends Exception
{
    public NodeMapException (String message)
    {
	super(message);
    }
}
