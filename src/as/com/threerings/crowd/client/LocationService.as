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

package com.threerings.crowd.client {

import flash.utils.ByteArray;
import com.threerings.io.TypedArray;
import com.threerings.crowd.client.LocationService;
import com.threerings.crowd.client.LocationService_MoveListener;
import com.threerings.crowd.data.LocationMarshaller_MoveMarshaller;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService_InvocationListener;

/**
 * An ActionScript version of the Java LocationService interface.
 */
public interface LocationService extends InvocationService
{
    // from Java interface LocationService
    function leavePlace (arg1 :Client) :void;

    // from Java interface LocationService
    function moveTo (arg1 :Client, arg2 :int, arg3 :LocationService_MoveListener) :void;
}
}
