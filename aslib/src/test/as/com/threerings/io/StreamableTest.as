//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.io {
import com.threerings.util.StringUtil;
import com.threerings.util.Name;
import flash.utils.ByteArray;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.ObjectInputStream;
import com.threerings.presents.peer.data.ClientInfo;

import asunit.framework.TestCase;

public class StreamableTest extends TestCase
{
    public function StreamableTest (name:String = null)
    {
        super(name);
    }

    public function testStreamingToSelf ():void
    {
        var sub:ASStreamableSubset = ASStreamableSubset.createWithJavaDefaults();
        var written:ByteArray = new ByteArray();
        var output:ObjectOutputStream = new ObjectOutputStream(written);
        output.writeObject(sub);
        written.position = 0;
        var input:ObjectInputStream = new ObjectInputStream(written);
        var read:ASStreamableSubset = input.readObject(ASStreamableSubset);
        assertTrue(sub.equals(read));
    }

    public function testStreamingFromJava () :void
    {
        var sub:ASStreamableSubset = ASStreamableSubset.createWithJavaDefaults();
        var javaData:ByteArray = StringUtil.unhexlate(
            "ffff0024636f6d2e746872656572696e67732e696f2e415353747265616d61626c65537562736574" +
            "01000200000003000000000000000440a0000040180000000000000007080100036f6e6500010000" +
            "0003010001010000000301020301000000030000000100000002000000030000000100000003fffe" +
            "00106a6176612e6c616e672e537472696e6700036f6e650002000374776f00020005746872656500" +
            "000001000000030002000374776f0002000132000200036f6e650002000131000200057468726565" +
            "000200013301000000030002000374776ffffd00116a6176612e6c616e672e496e74656765720000" +
            "0002000200036f6e65000300000001000200057468726565000300000003000000");
        var input:ObjectInputStream = new ObjectInputStream(javaData);
        var read :ASStreamableSubset = input.readObject(ASStreamableSubset);
        assertTrue(sub.equals(read));

    }
}
}
