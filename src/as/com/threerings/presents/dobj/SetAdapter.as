//
// $Id: SetAdapter.java 4191 2006-06-13 22:42:20Z ray $
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

package com.threerings.presents.dobj {

public class SetAdapter
    implements SetListener
{
    public function SetAdapter (
        addedFn :Function = null, updatedFn :Function = null,
        removedFn :Function = null)
    {
        _addedFn = addedFn;
        _updatedFn = updatedFn;
        _removedFn = removedFn;
    }

    // from SetListener
    public function entryAdded (event :EntryAddedEvent) :void
    {
        if (_addedFn != null) {
            _addedFn(event);
        }
    }

    // from SetListener
    public function entryUpdated (event :EntryUpdatedEvent) :void
    {
        if (_updatedFn != null) {
            _updatedFn(event);
        }
    }

    // from SetListener
    public function entryRemoved (event :EntryRemovedEvent) :void
    {
        if (_removedFn != null) {
            _removedFn(event);
        }
    }

    protected var _addedFn :Function;
    protected var _updatedFn :Function;
    protected var _removedFn :Function;
}
}
