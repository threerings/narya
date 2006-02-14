//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.media.animation;

import java.awt.Rectangle;

import java.util.ArrayList;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.Log;

/**
 * A utility class for positioning animations such that they don't overlap,
 * as best as possible.
 */
public class AnimationArranger
{
    /**
     * Position the specified animation so that it is not overlapping
     * any still-running animations previously passed to this method.
     */
    public void positionAvoidAnimation (Animation anim, Rectangle viewBounds)
    {
        Rectangle abounds = new Rectangle(anim.getBounds());
        ArrayList avoidables = (ArrayList) _avoidAnims.clone();
        // if we are able to place it somewhere, do so
        if (SwingUtil.positionRect(abounds, viewBounds, avoidables)) {
            anim.setLocation(abounds.x, abounds.y);
        }

        // add the animation to the list of avoidables
        _avoidAnims.add(anim);
        // keep an eye on it so that we can remove it when it's finished
        anim.addAnimationObserver(_avoidAnimObs);
    }

    /** The animations that other animations may wish to avoid. */
    protected ArrayList _avoidAnims = new ArrayList();

    /** Automatically removes avoid animations when they're done. */
    protected AnimationAdapter _avoidAnimObs = new AnimationAdapter() {
        public void animationCompleted (Animation anim, long when) {
            if (!_avoidAnims.remove(anim)) {
                Log.warning("Couldn't remove avoid animation?! " + anim + ".");
            }
        }
    };
}
