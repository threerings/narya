//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.presents.dobj;

import com.samskivert.util.StringUtil;

import com.threerings.presents.net.Transport;

/**
 * A message event is used to dispatch a message to all subscribers of a
 * distributed object without actually changing any of the fields of the
 * object. A message has a name, by which different subscribers of the
 * same object can distinguish their different messages, and an array of
 * arguments by which any contents of the message can be delivered.
 *
 * @see DObjectManager#postEvent
 */
public class MessageEvent extends NamedEvent
{
    /**
     * Constructs a new message event on the specified target object with
     * the supplied name and arguments.
     *
     * @param targetOid the object id of the object whose attribute has
     * changed.
     * @param name the name of the message event.
     * @param args the arguments for this message. This array should
     * contain only values of valid distributed object types.
     */
    public MessageEvent (int targetOid, String name, Object[] args)
    {
        this(targetOid, name, args, Transport.DEFAULT);
    }

    /**
     * Constructs a new message event on the specified target object with
     * the supplied name and arguments.
     *
     * @param targetOid the object id of the object whose attribute has
     * changed.
     * @param name the name of the message event.
     * @param args the arguments for this message. This array should
     * contain only values of valid distributed object types.
     * @param transport a hint as to the type of transport desired for the event.
     */
    public MessageEvent (int targetOid, String name, Object[] args, Transport transport)
    {
        super(targetOid, name, transport);
        _args = args;
    }

    /**
     * Constructs a blank instance of this event in preparation for
     * unserialization from the network.
     */
    public MessageEvent ()
    {
    }

    /**
     * Returns the arguments to this message.
     */
    public Object[] getArgs ()
    {
        return _args;
    }

    /**
     * Replaces the arguments associated with this message event.
     * <em>Note:</em> this should only be called on events that have not
     * yet been dispatched into the distributed object system.
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
