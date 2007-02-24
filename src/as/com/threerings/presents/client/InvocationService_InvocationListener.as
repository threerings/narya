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

package com.threerings.presents.client {

/**
 * Invocation service methods that require a response should take a
 * listener argument that can be notified of request success or
 * failure. The listener argument should extend this interface so that
 * generic failure can be reported in all cases. For example:
 *
 * <pre>
 * // Used to communicate responses to <code>moveTo</code> requests.
 * public interface MoveListener extends InvocationListener
 * {
 *     // Called in response to a successful <code>moveTo</code>
 *     // request.
 *     public void moveSucceeded (PlaceConfig config);
 * }
 * </pre>
 */
public interface InvocationService_InvocationListener
{
    /**
     * Called to report request failure. If the invocation services
     * system detects failure of any kind, it will report it via this
     * callback. Particular services may also make use of this
     * callback to report failures of their own, or they may opt to
     * define more specific failure callbacks.
     */
    function requestFailed (cause :String) :void;
}
}
