//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

import java.util.ArrayList;

import com.threerings.presents.client.InvocationService;

/**
 * Maintains a list of result listeners, dispatching the eventual actual result or failure to them
 * all as if they were a single listener.
 */
public class ResultListenerList extends ArrayList<InvocationService.ResultListener>
    implements InvocationService.ResultListener
{
    // from InvocationService.ResultListener
    public void requestProcessed (Object result)
    {
        for (InvocationService.ResultListener listener : this) {
            listener.requestProcessed(result);
        }
    }

    // from InvocationService.ResultListener
    public void requestFailed (String cause)
    {
        for (InvocationService.ResultListener listener : this) {
            listener.requestFailed(cause);
        }
    }
}
