//
// $Id: ParlorContext.java,v 1.3 2004/08/27 02:20:16 mdb Exp $
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

package com.threerings.parlor.util;

import com.threerings.crowd.util.CrowdContext;
import com.threerings.parlor.client.ParlorDirector;

/**
 * The parlor context provides access to the various managers, etc. that
 * are needed by the parlor client code.
 */
public interface ParlorContext extends CrowdContext
{
    /**
     * Returns a reference to the parlor director.
     */
    public ParlorDirector getParlorDirector ();
}
