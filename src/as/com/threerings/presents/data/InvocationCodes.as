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

package com.threerings.presents.data {

/**
 * The invocation codes interface provides codes that are commonly used by invocation service
 * implementations. It is implemented as an interface so that were an invocation service to desire
 * to build on two or more other services, it can provide a codes interface that inherits from all
 * of the services that it extends.
 */
public class InvocationCodes
{
    /** Defines a global invocation services group that can be used by clients and services that do
     * not care to make a distinction between groups of invocation services. */
    public static const GLOBAL_GROUP :String = "presents";

    /** An error code returned to clients when a service cannot be performed because of some
     * internal server error that we couldn't explain in any meaningful way (things like null
     * pointer exceptions). */
    public static const INTERNAL_ERROR :String = "m.internal_error";

    /** An error code returned to clients when a service cannot be performed because the requesting
     * client does not have the proper access. */
    public static const ACCESS_DENIED :String = "m.access_denied";
}
}
