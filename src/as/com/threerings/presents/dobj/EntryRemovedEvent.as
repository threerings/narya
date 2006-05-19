package com.threerings.presents.dobj {
    
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.util.StringBuilder;

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
            targetOid :int = 0, name :String = null, key :Object = null,
            oldEntry :DSet_Entry = null)
    {
        super(targetOid, name);
        _key = key;
        if (oldEntry != null) {
            _oldEntry = oldEntry;
        }
    }

    /**
     * Returns the key that identifies the entry that has been removed.
     */
    public function getKey () :Object
    {
        return _key;
    }

    /**
     * Returns the entry that was in the set prior to being updated.
     */
    public function getOldEntry () :DSet_Entry
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
                Log.getLog(this).warning("No matching entry to remove " +
                    "[key=" + _key + ", set=" + dset + "].");
                return false;
            }
        }
        return true;
    }

    // documentation inherited
    protected override function notifyListener (listener :Object) :void
    {
        if (listener is SetListener) {
            listener.entryRemoved(this);
        }
    }

    // documentation inherited
    protected override function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("ELREM:");
        super.toStringBuf(buf);
        buf.append(", key=", _key);
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(_key);
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _key = ins.readObject();
    }

    protected var _key :Object;
    protected var _oldEntry :DSet_Entry = UNSET_OLD_ENTRY;
}
}
