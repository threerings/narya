#include "presents/stable.h"
#include "EntryEvent.h"

using namespace presents::dobj;

DEFINE_STREAMABLE("com.threerings.presents.dobj.EntryEvent", EntryEvent);

void EntryEvent::readObject (ObjectInputStream& in)
{
    presents::dobj::NamedEvent::readObject(in);
}

void EntryEvent::writeObject (ObjectOutputStream& out) const
{
    presents::dobj::NamedEvent::writeObject(out);
}
