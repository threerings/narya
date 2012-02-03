#include "presents/stable.h"
#include "EntryAddedEvent.h"

using namespace presents::dobj;

DEFINE_STREAMABLE("com.threerings.presents.dobj.EntryAddedEvent", EntryAddedEvent);

void EntryAddedEvent::readObject (ObjectInputStream& in)
{
    presents::dobj::EntryEvent::readObject(in);
    entry = boost::static_pointer_cast<Streamable>(in.readObject());
}

void EntryAddedEvent::writeObject (ObjectOutputStream& out) const
{
    presents::dobj::EntryEvent::writeObject(out);
    out.writeObject(entry);
}
