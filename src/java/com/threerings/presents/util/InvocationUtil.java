//
// $Id$
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

package com.threerings.presents.util;

import java.util.Collection;
import java.util.Iterator;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.client.InvocationService.ResultListener;

/**
 * Invocation service related utilities.
 */
public class InvocationUtil
{
    /**
     * Safely iterates over the supplied collection of {@link
     * ResultListener}s and calls {@link ResultListener#requestCompleted}
     * on them, catching any exceptions they might thow and logging an
     * error, then proceeding to the next listener.
     */
    public static void safeNotify (Collection listeners, Object result)
    {
        for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
            ResultListener rl = (ResultListener)iter.next();
            try {
                rl.requestProcessed(result);
            } catch (Throwable t) {
                Log.warning("Result listener choked in requestProcessed() " +
                            "[listener=" + StringUtil.safeToString(rl) + "].");
                Log.logStackTrace(t);
            }
        }
    }
}
