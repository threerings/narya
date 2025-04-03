//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * Used to validate distributed object subscription requests and event
 * dispatches.
 *
 * @see DObject#setAccessController
 */
public interface AccessController
{
    /**
     * Should return true if the supplied subscriber is allowed to
     * subscribe to the specified object.
     */
    boolean allowSubscribe (DObject object, Subscriber<?> subscriber);

    /**
     * Should return true if the supplied event is legal for dispatch on
     * the specified distributed object.
     */
    boolean allowDispatch (DObject object, DEvent event);
}
