//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * The various listener interfaces (e.g. {@link EventListener}, {@link AttributeChangeListener},
 * etc.) all extend this base interface so that the distributed object can check to make sure when
 * an object is adding itself as a listener of some sort that it actually implements at least one
 * of the listener interfaces. Thus, all listener interfaces must extend this one.
 */
public interface ChangeListener
{
}
