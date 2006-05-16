package com.threerings.util {

import mx.utils.ObjectUtil;

/**
 * A Hashtable implementation that utilizes ObjectUtil.compare and
 * ObjectUtil.toString for hashing keys that are non-simple and do
 * not implement Hashable.
 */
public class HashMap extends Hashtable
{
    public function HashMap (loadFactor :Number = 1.75)
    {
        super(loadFactor,
            function (o1 :Object, o2 :Object) :Boolean {
                return (0 == ObjectUtil.compare(o1, o2));
            },
            ObjectUtil.toString);
    }
}
}
