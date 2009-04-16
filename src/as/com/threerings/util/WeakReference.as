//
// $Id$

package com.threerings.util {

import flash.utils.Dictionary;

/**
 * A weak reference. At some point in the future the referent might not be available
 * anymore.
 */
public class WeakReference
{
    /**
     * No, you cannot store undefined here.
     */
    public function WeakReference (referant :Object)
    {
        _ref[referant] = true;
    }

    /**
     * Return the referant, or undefined if it's been collected.
     */
    public function get () :*
    {
        for (var k :* in _ref) {
            return k;
        }
        return undefined;
    }

    /** The only way to have a weak reference in actionscript. */
    protected var _ref :Dictionary = new Dictionary(true);
}
}
