//
// $Id: InvocationDecoder.java,v 1.2 2004/08/27 02:20:18 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.client;

import com.samskivert.util.StringUtil;
import com.threerings.presents.Log;

/**
 * Provides the basic functionality used to dispatch invocation
 * notification events.
 */
public abstract class InvocationDecoder
{
    /** The receiver for which we're decoding and dipatching
     * notifications. */
    public InvocationReceiver receiver;

    /**
     * Returns the generated hash code that is used to identify this
     * invocation notification service.
     */
    public abstract String getReceiverCode ();

    /**
     * Dispatches the specified method to our receiver.
     */
    public void dispatchNotification (int methodId, Object[] args)
    {
        Log.warning("Requested to dispatch unknown method " +
                    "[receiver=" + receiver + ", methodId=" + methodId +
                    ", args=" + StringUtil.toString(args) + "].");
    }
}
