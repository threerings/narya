package com.threerings.presents.dobj {

import flash.util.StringBuilder;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.Log;

/**
 * An entry updated event is dispatched when an entry of a {@link DSet} is
 * updated. It can also be constructed to request the update of an entry
 * and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryUpdatedEvent extends NamedEvent
{
    /**
     * Constructs a new entry updated event on the specified target object
     * for the specified set name and with the supplied updated entry.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an entry.
     * @param name the name of the attribute in which to update the
     * specified entry.
     * @param entry the entry to update.
     * @param oldEntry the previous value of the entry.
     */
    public function EntryUpdatedEvent (
            targetOid :int, name :String, entry :DSetEntry, oldEntry :DSetEntry)
    {
        super(targetOid, name);
        _entry = entry;
        _oldEntry = oldEntry;
    }

    /**
     * Returns the entry that has been updated.
     */
    public function getEntry () :DSetEntry
    {
        return _entry;
    }

    /**
     * Returns the entry that was in the set prior to being updated.
     */
    public function getOldEntry ():DSetEntry
    {
        return _oldEntry;
    }

    /**
     * Applies this event to the object.
     */
    public override function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        // only apply the change if we haven't already
        if (_oldEntry == UNSET_OLD_ENTRY) {
            var dset :DSet = target[_name];
            // fetch the previous value for interested callers
            _oldEntry = dset.update(_entry);
            if (_oldEntry == null) {
                // complain if we didn't update anything
                Log.warning("No matching entry to update [entry=" + this +
                            ", set=" + dset + "].");
                return false;
            }
        }
        return true;
    }

    // documentation inherited
    internal override function notifyListener (listener :Object) :void
    {
        if (listener is SetListener) {
            listener.entryUpdated(this);
        }
    }

    // documentation inherited
    protected override function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("ELUPD:");
        super.toStringBuf(buf);
        buf.append(", entry=", _entry);
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(_entry);
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _entry = (ins.readObject() as DSetEntry);
    }

    protected var _entry :DSetEntry;
    protected var _oldEntry :DSetEntry = UNSET_OLD_ENTRY;
}
}
