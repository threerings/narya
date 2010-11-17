#include "presents/stable.h"
#include "EntryUpdatedEvent.h"

using namespace presents::dobj;

DEFINE_STREAMABLE("com.threerings.presents.dobj.EntryUpdatedEvent", EntryUpdatedEvent);

void EntryUpdatedEvent::readObject (ObjectInputStream& in)
{
    presents::dobj::EntryEvent::readObject(in);
    entry = boost::static_pointer_cast<Streamable>(in.readObject());
}

void EntryUpdatedEvent::writeObject (ObjectOutputStream& out) const
{
    presents::dobj::EntryEvent::writeObject(out);
    out.writeObject(entry);
}
