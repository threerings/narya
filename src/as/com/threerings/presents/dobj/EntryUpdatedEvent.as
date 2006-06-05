package com.threerings.presents.dobj {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.util.StringBuilder;

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
            targetOid :int = 0, name :String = null, entry :DSet_Entry = null,
            oldEntry :DSet_Entry = null)
    {
        super(targetOid, name);
        _entry = entry;
        if (oldEntry != null) {
            _oldEntry = oldEntry;
        }
    }

    /**
     * Returns the entry that has been updated.
     */
    public function getEntry () :DSet_Entry
    {
        return _entry;
    }

    /**
     * Returns the entry that was in the set prior to being updated.
     */
    public function getOldEntry ():DSet_Entry
    {
        return _oldEntry;
    }

    /**
     * Applies this event to the object.
     */
    override public function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        // only apply the change if we haven't already
        if (_oldEntry == UNSET_OLD_ENTRY) {
            var dset :DSet = (target[_name] as DSet);
            // fetch the previous value for interested callers
            _oldEntry = dset.update(_entry);
            if (_oldEntry == null) {
                // complain if we didn't update anything
                Log.getLog(this).warning("No matching entry to update " +
                    "[entry=" + this + ", set=" + dset + "].");
                return false;
            }
        }
        return true;
    }

    // documentation inherited
    override protected function notifyListener (listener :Object) :void
    {
        if (listener is SetListener) {
            listener.entryUpdated(this);
        }
    }

    // documentation inherited
    override protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("ELUPD:");
        super.toStringBuf(buf);
        buf.append(", entry=", _entry);
    }

    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(_entry);
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _entry = (ins.readObject() as DSet_Entry);
    }

    protected var _entry :DSet_Entry;
    protected var _oldEntry :DSet_Entry = UNSET_OLD_ENTRY;
}
}
