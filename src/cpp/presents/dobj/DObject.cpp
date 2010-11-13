#include "presents/stable.h"
#include "DObject.h"

using namespace presents::dobj;

DEFINE_STREAMABLE("com.threerings.presents.dobj.DObject", DObject);

void DObject::readObject (ObjectInputStream& in)
{
    oid = int32(in.readInt());
}

void DObject::writeObject (ObjectOutputStream& out) const
{
    out.writeInt(oid);
}
