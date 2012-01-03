//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
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

package com.threerings.presents.data;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationReceiver;
import com.threerings.presents.client.RegistrationService;

/**
 * Provides the implementation of the {@link RegistrationService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.cpp.GenCPPServiceTask"},
           comments="Derived from RegistrationService.java.")
public class RegistrationMarshaller extends InvocationMarshaller<ClientObject>
    implements RegistrationService
{
    /** The method id used to dispatch {@link #registerReceiver} requests. */
    public static final int REGISTER_RECEIVER = 1;

    // from interface RegistrationService
    public void registerReceiver (InvocationReceiver.Registration arg1)
    {
        sendRequest(REGISTER_RECEIVER, new Object[] {
            arg1
        });
    }
}
