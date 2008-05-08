//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2008 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.bureau.client {

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * An ActionScript version of the Java BureauService interface.
 */
public interface BureauService extends InvocationService
{
    // from Java interface BureauService
    function agentCreated (arg1 :Client, arg2 :int) :void;

    // from Java interface BureauService
    function agentCreationFailed (arg1 :Client, arg2 :int) :void;

    // from Java interface BureauService
    function agentDestroyed (arg1 :Client, arg2 :int) :void;

    // from Java interface BureauService
    function bureauInitialized (arg1 :Client, arg2 :String) :void;
}
}
