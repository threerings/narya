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

package com.threerings.presents.client;

import com.samskivert.util.Log;

/**
 * Implements the basic {@link InvocationService.InvocationListener} and
 * logs the failure.
 */
public class LoggingListener
    implements InvocationService.InvocationListener
{
    /**
     * Constructs a listener that will report the supplied error message
     * along with the reason for failure to the supplied log object.
     */
    public LoggingListener (Log log, String errmsg)
    {
        _log = log;
        _errmsg = errmsg;
    }

    // documentation inherited from interface
    public void requestFailed (String reason)
    {
        _log.warning(_errmsg + " [reason=" + reason + "].");
    }

    protected Log _log;
    protected String _errmsg;
}
