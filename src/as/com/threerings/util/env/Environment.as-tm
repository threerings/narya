//
// $Id: $
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

package com.threerings.util.env {

import flash.utils.getQualifiedClassName;
import flash.utils.getDefinitionByName;
import avmplus.*;

/**
 * Tamarin specific implementation of Environment.
 */
public class Environment
{
    /**
     * Return an array containing all the public field names of the object
     */
    public static function enumerateFields (obj :Object) :Array
    {
        if (obj != null) {
            return Domain.currentDomain.getVariables(obj);
        }
        return new Array();
    }

    /**
     * Returns true if an object of type srcClass is a subclass of or
     * implements the interface represented by the asClass paramter.
     *
     * <code>
     * if (ClassUtil.isAssignableAs(Streamable, someClass)) {
     *     var s :Streamable = (new someClass() as Streamable);
     * </code>
     */
    public static function isAssignableAs (asClass :Class, srcClass :Class) :Boolean
    {
        if (asClass != null && srcClass != null) {
            return Domain.currentDomain.isAssignableAs(asClass, srcClass);
        }
        return false;
    }
}
}
