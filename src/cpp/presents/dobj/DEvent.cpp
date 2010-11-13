#include "presents/stable.h"
#include "DEvent.h"

using namespace presents::dobj;

DEFINE_STREAMABLE("com.threerings.presents.dobj.DEvent", DEvent);

void DEvent::readObject (ObjectInputStream& in)
{
    toid = int32(in.readInt());
}

void DEvent::writeObject (ObjectOutputStream& out) const
{
    out.writeInt(toid);
}
