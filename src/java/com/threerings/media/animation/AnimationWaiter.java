//
// $Id: AnimationWaiter.java,v 1.5 2004/08/27 02:12:38 mdb Exp $
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

package com.threerings.media.animation;

/**
 * An abstract class that simplifies a common animation usage case in
 * which a number of animations are created and the creator would like to
 * be able to perform specific actions when each animation has completed
 * (see {@link #animationDidFinish} and/or when all animations are
 * finished (see {@link #allAnimationsFinished}.)
 */
public abstract class AnimationWaiter
    implements AnimationObserver
{
    /**
     * Adds an animation to the animation waiter for observation.
     */
    public void addAnimation (Animation anim)
    {
        anim.addAnimationObserver(this);
        _animCount++;
    }

    /**
     * Adds the supplied animations to the animation waiter for
     * observation.
     */
    public void addAnimations (Animation[] anims)
    {
        int acount = anims.length;
        for (int ii = 0; ii < acount; ii++) {
            addAnimation(anims[ii]);
        }
    }

    // documentation inherited from interface
    public void animationStarted (Animation anim, long when)
    {
    }

    // documentation inherited from interface
    public void animationCompleted (Animation anim, long when)
    {
        // note that the animation is finished
        animationDidFinish(anim);
        _animCount--;

        // let derived classes know when all is done
        if (_animCount == 0) {
            allAnimationsFinished();
        }
    }

    /**
     * Called when an animation being observed by the waiter has
     * completed its business.  Derived classes may wish to override
     * this method to engage in their unique antics.
     */
    protected void animationDidFinish (Animation anim)
    {
        // nothing for now
    }

    /**
     * Called when all animations being observed by the waiter have
     * completed their business.  Derived classes may wish to override
     * this method to engage in their unique antics.
     */
    protected void allAnimationsFinished ()
    {
        // nothing for now
    }

    /** The number of animations. */
    protected int _animCount;
}
