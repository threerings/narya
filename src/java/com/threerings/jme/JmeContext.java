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

package com.threerings.jme;

import com.jme.input.InputHandler;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;

import com.jme.bui.event.InputDispatcher;

/**
 * Provides access to the various bits needed by things that operate in
 * JME land.
 */
public interface JmeContext
{
    /** Returns the renderer being used to draw everythign. */
    public Renderer getRenderer ();

    /** Returns the root of our scene graph. */
    public Node getRoot ();

//     /** Returns our main (unbuffered) input handler. */
//     public InputHandler getInputHandler ();

//     /** Returns our buffered input handler. */
//     public InputHandler getBufferedInputHandler ();

    /** Returns our main input dispatcher. */
    public InputDispatcher getInputDispatcher ();
}
