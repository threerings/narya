//
// $Id: FrameParticipant.java,v 1.4 2004/08/27 02:12:37 mdb Exp $
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

package com.threerings.media;

import java.awt.Component;

/**
 * Provides a mechanism for participating in the frame tick managed by the
 * {@link FrameManager}.
 */
public interface FrameParticipant
{
    /**
     * This is called on all registered frame participants, one for every
     * frame. Following the tick the interface will be rendered, so
     * participants can prepare themselves for their upcoming render in
     * this method (making use of the timestamp provided for the frame if
     * choreography is desired between different participants).
     */
    public void tick (long tickStamp);

    /**
     * Called immediately prior to {@link #getComponent} and then {@link
     * Component#paint} on said component, to determine whether or not
     * this frame participant needs to be painted.
     */
    public boolean needsPaint ();

    /**
     * If a frame participant wishes also to be actively rendered every
     * frame rather than use passive rendering (which for Swing, at least,
     * is hijacked when using the frame manager such that we take care of
     * repainting dirty Swing components every frame into our off-screen
     * buffer), it can return a component here which will have {@link
     * Component#paint} called on it once per frame with a translated but
     * unclipped graphics object.
     *
     * <p> Because clipping is expensive in terms of rectangle object
     * allocation, frame participants are given the opportunity to do
     * their own clipping because they are likely to want to clip to a
     * more fine grained region than their entire bounds. If a particpant
     * does not wish to be actively rendered, it can safely return null.
     */
    public Component getComponent ();
}
