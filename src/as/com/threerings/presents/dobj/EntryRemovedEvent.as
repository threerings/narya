package com.threerings.presents.dobj {

import flash.util.StringBuilder;
    
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;


/**
 * An entry removed event is dispatched when an entry is removed from a
 * {@link DSet} attribute of a distributed object. It can also be
 * constructed to request the removal of an entry from a set and posted to
 * the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryRemovedEvent extends NamedEvent
{
    /**
     * Constructs a new entry removed event on the specified target object
     * with the supplied set attribute name and entry key to remove.
     *
     * @param targetOid the object id of the object from whose set we will
     * remove an entry.
     * @param name the name of the attribute from which to remove the
     * specified entry.
     * @param key the entry key that identifies the entry to remove.
     * @param oldEntry the previous value of the entry.
     */
    public function EntryRemovedEvent (
            targetOid :int, name :String, key :Comparable, oldEntry :DSetEntry)
    {
        super(targetOid, name);
        _key = key;
        _oldEntry = oldEntry;
    }

    /**
     * Returns the key that identifies the entry that has been removed.
     */
    public function getKey () :Comparable
    {
        return _key;
    }

    /**
     * Returns the entry that was in the set prior to being updated.
     */
    public function getOldEntry () :DSetEntry
    {
        return _oldEntry;
    }

    /**
     * Applies this event to the object.
     */
    public override function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        if (_oldEntry == UNSET_OLD_ENTRY) {
            var dset :DSet = target[_name];
            // remove, fetch the previous value for interested callers
            _oldEntry = dset.removeKey(_key);
            if (_oldEntry == null) {
                // complain if there was actually nothing there
                trace("No matching entry to remove [key=" + _key +
                            ", set=" + set + "].");
                return false;
            }
        }
        return true;
    }

    // documentation inherited
    protected override function notifyListener (listener :*) :void
    {
        if (listener is SetListener) {
            listener.entryRemoved(this);
        }
    }

    // documentation inherited
    protected override function toString (buf :StringBuilder)
    {
        buf.append("ELREM:");
        super.toString(buf);
        buf.append(", key=", _key);
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeField(_key);
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _key = ins.readField(Comparable);
    }

    protected var _key :Comparable;
    protected var _oldEntry :DSetEntry = UNSET_OLD_ENTRY;
}
}
