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

import flash.utils.ByteArray;
import com.threerings.util.*; // for Float, Integer, etc.

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService_InvocationListener;
import com.threerings.presents.client.TimeBaseService;
import com.threerings.presents.client.TimeBaseService_GotTimeBaseListener;
import com.threerings.presents.data.TimeBaseMarshaller_GotTimeBaseMarshaller;

/**
 * An ActionScript version of the Java TimeBaseService_GotTimeBaseListener interface.
 */
public interface TimeBaseService_GotTimeBaseListener
    extends InvocationService_InvocationListener
{
    // from Java TimeBaseService_GotTimeBaseListener
    function gotTimeOid (arg1 :int) :void
}
}
