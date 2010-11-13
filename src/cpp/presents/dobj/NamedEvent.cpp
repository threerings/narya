#include "presents/stable.h"
#include "NamedEvent.h"

using namespace presents::dobj;

DEFINE_STREAMABLE("com.threerings.presents.dobj.NamedEvent", NamedEvent);

void NamedEvent::readObject (ObjectInputStream& in)
{
    presents::dobj::DEvent::readObject(in);
    name = in.readField< utf8 >();
}

void NamedEvent::writeObject (ObjectOutputStream& out) const
{
    presents::dobj::DEvent::writeObject(out);
    out.writeField(name);
}
