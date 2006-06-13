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

package com.threerings.media.util;

/**
 * This interface should be implemented by classes that wish to register
 * actions to be monitored by the {@link PerformanceMonitor} class.
 */
public interface PerformanceObserver
{
    /**
     * This method is called by the {@link PerformanceMonitor} class
     * whenever an action's requested time interval between checkpoints
     * has expired.
     *
     * @param name the action name.
     * @param ticks the ticks since the last checkpoint.
     */
    public void checkpoint (String name, int ticks);
}
