//
// $Id: ChangeListener.java,v 1.1 2002/02/03 04:38:05 mdb Exp $

package com.threerings.presents.dobj;

/**
 * The various listener interfaces (e.g. {@link EventListener}, {@link
 * AttributeChangeListener}, etc.) all extend this base interface so that
 * the distributed object can check to make sure when an object is adding
 * itself as a listener of some sort that it actually implements at least
 * one of the listener interfaces. Thus, all listener interfaces must
 * extend this one.
 */
public interface ChangeListener
{
}
