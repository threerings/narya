//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

import com.samskivert.util.StringUtil;

/**
 * A message event is used to dispatch a message to all subscribers of a distributed object without
 * actually changing any of the fields of the object. A message has a name, by which different
 * subscribers of the same object can distinguish their different messages, and an array of
 * arguments by which any contents of the message can be delivered.
 *
 * @see DObjectManager#postEvent
 */
public class MessageEvent extends NamedEvent
{
    /**
     * Constructs a new message event on the specified target object with the supplied name and
     * arguments.
     *
     * @param targetOid the object id of the object whose attribute has changed.
     * @param name the name of the message event.
     * @param args the arguments for this message. This array should contain only values of valid
     * distributed object types.
     */
    public MessageEvent (int targetOid, String name, Object[] args)
    {
        super(targetOid, name);
        _args = args;
    }

    /**
     * Returns the arguments to this message.
     */
    public Object[] getArgs ()
    {
        return _args;
    }

    /**
     * Replaces the arguments associated with this message event. <em>Note:</em> this should only
     * be called on events that have not yet been dispatched into the distributed object system.
     */
    public void setArgs (Object[] args)
    {
        _args = args;
    }

    @Override
    public boolean applyToObject (DObject target)
        throws ObjectAccessException
    {
        // nothing to do here
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        if (listener instanceof MessageListener) {
            ((MessageListener)listener).messageReceived(this);
        }
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        buf.append("MSG:");
        super.toString(buf);
        buf.append(", args=").append(StringUtil.toString(_args));
    }

    protected Object[] _args;
}
