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

package com.threerings.tools.xml;

import org.apache.commons.digester.Digester;

/**
 * Used to define rule sets that can be nested within other rule sets. For
 * example, say you have a "scene" object definition like so:
 *
 * <p> (Note that in the examples square brackets are used instead of
 * angle brackets to simplify my life when composing the documentation.)
 *
 * <pre>
 * [scene name="Foo" version=5]
 * [/scene]
 * </pre>
 *
 * This scene is extended with some auxiliary data defined by libraries
 * which can parse and generate XML for their auxiliary objects:
 *
 * <pre>
 * [scene sceneId=1 name="Foo" version=5]
 *   [spot]
 *     [portal portalId=1 x=1 y=1 targetSceneId=2/]
 *     [portal portalId=2 x=15 y=3 targetSceneId=3/]
 *     [portal portalId=3 x=9 y=6 targetSceneId=4/]
 *   [/spot]
 *   [miso]
 *     [object tileId=878172 x=4 y=13 action="cluck"/]
 *     [object tileId=123843 x=18 y=23 action="bark"/]
 *   [/miso]
 * [/scene]
 * </pre>
 *
 * The spot and miso services can define nestable rule sets which will be
 * handed to the scene services who will instruct them to add their rule
 * instances with a prefix of <code>scene.spot</code> and
 * <code>scene.miso</code> respectively. They then happily parse their
 * auxiliary objects without knowing that they have been nested inside
 * some larger structure.
 *
 * <p> The nestable ruleset should then leave a single object on the
 * digester stack that the enclosing entity can grab.
 *
 * <p> This isn't proper use of XML, but it solves the problem at hand in
 * an easily extensible manner.
 */
public interface NestableRuleSet
{
    /**
     * Returns the name of the nested object's outer element so that the
     * parent parser can use it to compose the total path prefix.
     */
    public String getOuterElement ();

    /**
     * Instructs this ruleset to add its rules such that it parses its
     * object from the specified path prefix. The outer element returned
     * by {@link #getOuterElement} will have been included in the path
     * prefix.
     */
    public void addRuleInstances (String prefix, Digester digester);
}
