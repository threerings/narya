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

package com.threerings.jme.sprite;

import com.jme.scene.Node;
import com.jme.scene.Spatial;

import com.samskivert.util.ObserverList;

/**
 * Represents a visual entity that one controls as a single unit. Sprites
 * can be made to follow paths which is one of their primary reasons for
 * existence.
 */
public class Sprite extends Node
{
    /**
     * Walks down the hierarchy provided setting the animation speed on
     * any controllers found along the way.
     */
    public static void setAnimationSpeed (Spatial spatial, float speed)
    {
        for (int ii = 0; ii < spatial.getControllers().size(); ii++) {
            spatial.getController(ii).setSpeed(speed);
        }
        if (spatial instanceof Node) {
            Node node = (Node)spatial;
            for (int ii = 0; ii < node.getQuantity(); ii++) {
                setAnimationSpeed(node.getChild(ii), speed);
            }
        }
    }

    /**
     * Walks down the hierarchy provided turning on or off any controllers
     * found along the way.
     */
    public static void setAnimationActive (Spatial spatial, boolean active)
    {
        for (int ii = 0; ii < spatial.getControllers().size(); ii++) {
            spatial.getController(ii).setActive(active);
        }
        if (spatial instanceof Node) {
            Node node = (Node)spatial;
            for (int ii = 0; ii < node.getQuantity(); ii++) {
                setAnimationActive(node.getChild(ii), active);
            }
        }
    }

    public Sprite ()
    {
        super("");
        setName("sprite:" + hashCode());
    }

    /**
     * Adds an observer to this sprite. Observers are notified when path
     * related events take place.
     */
    public void addObserver (SpriteObserver obs)
    {
        if (_observers == null) {
            _observers = new ObserverList(ObserverList.FAST_UNSAFE_NOTIFY);
        }
        _observers.add(obs);
    }

    /**
     * Removes the specified observer from this sprite.
     */
    public void removeObserver (SpriteObserver obs)
    {
        if (_observers != null) {
            _observers.remove(obs);
        }
    }

    /**
     * Returns true if this sprite is moving along a path, false if not.
     */
    public boolean isMoving ()
    {
        return _path != null;
    }

    /**
     * Instructs this sprite to move along the specified path. Any
     * currently executing path will be cancelled.
     */
    public void move (Path path)
    {
        // if there's a previous path, let it know that it's going away
        cancelMove();

        // save off this path
        _path = path;
        addController(_path);
    }

    /**
     * Cancels any currently executing path. Any registered observers will
     * be notified of the cancellation.
     */
    public void cancelMove ()
    {
        if (_path != null) {
            Path oldpath = _path;
            _path = null;
            oldpath.wasRemoved();
            if (_observers != null) {
                _observers.apply(new CancelledOp(this, oldpath));
            }
        }
    }

    /**
     * Called by the active path when it has completed. <em>Note:</em>
     * don't call this method unless you are implementing a {@link Path}.
     */
    public void pathCompleted ()
    {
        Path oldpath = _path;
        _path = null;
        removeController(oldpath);
        oldpath.wasRemoved();
        if (_observers != null) {
            _observers.apply(new CompletedOp(this, oldpath));
        }
    }

    /**
     * Configures the speed of all controllers in our model hierarchy.
     */
    public void setAnimationSpeed (float speed)
    {
        setAnimationSpeed(this, speed);
    }

    /**
     * Configures the speed of all controllers in our model hierarchy.
     */
    public void setAnimationActive (boolean active)
    {
        setAnimationActive(this, active);
    }

    /** Used to dispatch {@link PathObserver#pathCancelled}. */
    protected static class CancelledOp implements ObserverList.ObserverOp
    {
        public CancelledOp (Sprite sprite, Path path) {
            _sprite = sprite;
            _path = path;
        }

        public boolean apply (Object observer) {
            if (observer instanceof PathObserver) {
                ((PathObserver)observer).pathCancelled(_sprite, _path);
            }
            return true;
        }

        protected Sprite _sprite;
        protected Path _path;
    }

    /** Used to dispatch {@link PathObserver#pathCompleted}. */
    protected static class CompletedOp implements ObserverList.ObserverOp
    {
        public CompletedOp (Sprite sprite, Path path) {
            _sprite = sprite;
            _path = path;
        }

        public boolean apply (Object observer) {
            if (observer instanceof PathObserver) {
                ((PathObserver)observer).pathCompleted(_sprite, _path);
            }
            return true;
        }

        protected Sprite _sprite;
        protected Path _path;
    }

    protected ObserverList _observers;
    protected Path _path;
}
