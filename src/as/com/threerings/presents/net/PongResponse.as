//
// $Id$
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

package com.threerings.presents.net {

import flash.utils.getTimer;

import com.threerings.util.Long;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

public class PongResponse extends DownstreamMessage
{
    public function PongResponse ()
    {
        super();
    }

    public function getPackStamp () :Long
    {
        return _packStamp;
    }

    public function getProcessDelay () :int
    {
        return _processDelay;
    }

    public function getUnpackStamp () :uint
    {
        return _unpackStamp;
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        _unpackStamp = getTimer();
        super.readObject(ins);

        // TODO: Figure out how we're really going to cope with longs
        _packStamp = new Long(0);
        ins.readBareObject(_packStamp);

        _processDelay = ins.readInt();
    }

    protected var _packStamp :Long;

    protected var _processDelay :int;

    protected var _unpackStamp :uint;
}
}
