package com.threerings.util {

import mx.collections.ListCollectionView;

/**
 * Various collections-related utility methods.
 */
public class Collections
{
    /**
     * Randomly shuffle the elements in the specified list.
     */
    public static function shuffle (collection :ListCollectionView) :void
    {
        // starting from the end of the list, repeatedly swap the element
        // in question with a random element previous to it up
        // to and including itself
        for (var ii :int  = collection.length - 1; ii > 0; ii--) {
            var idx :int = int(Math.random() * (ii + 1))
            var item :Object = collection.getItemAt(idx);
            collection.setItemAt(collection.setItemAt(item, ii), idx);
        }
    }
}
}
