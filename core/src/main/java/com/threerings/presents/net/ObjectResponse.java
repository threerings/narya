//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.net;

import com.threerings.presents.dobj.DObject;

/**
 * Contains a distributed object to which the client has subscribed.
 *
 * @param <T> the type of object delivered by the response.
 */
public class ObjectResponse<T extends DObject>
    extends DownstreamMessage
{
    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public ObjectResponse ()
    {
        super();
    }

    /**
     * Constructs an object response with the supplied distributed object.
     */
    public ObjectResponse (T dobj)
    {
        _dobj = dobj;
    }

    public T getObject ()
    {
        return _dobj;
    }

    @Override
    public String toString ()
    {
        return "[type=ORSP, msgid=" + messageId + ", obj=" + _dobj + "]";
    }

    /** The object which is associated with this response. */
    protected T _dobj;
}
