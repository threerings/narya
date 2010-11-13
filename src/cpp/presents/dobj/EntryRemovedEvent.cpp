#include "presents/stable.h"
#include "EntryRemovedEvent.h"

using namespace presents::dobj;

DEFINE_STREAMABLE("com.threerings.presents.dobj.EntryRemovedEvent", EntryRemovedEvent);

void EntryRemovedEvent::readObject (ObjectInputStream& in)
{
    presents::dobj::NamedEvent::readObject(in);
    key = boost::static_pointer_cast<Streamable>(in.readObject());
}

void EntryRemovedEvent::writeObject (ObjectOutputStream& out) const
{
    presents::dobj::NamedEvent::writeObject(out);
    out.writeObject(key);
}
