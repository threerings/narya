#include "presents/stable.h"
#include "InvocationRequestEvent.h"

using namespace presents::dobj;

DEFINE_STREAMABLE("com.threerings.presents.dobj.InvocationRequestEvent", InvocationRequestEvent);

void InvocationRequestEvent::readObject (ObjectInputStream& in)
{
    presents::dobj::DEvent::readObject(in);
    invCode = int32(in.readInt());
    methodId = int8(in.readByte());
    args = in.readField<  std::vector< Shared<Streamable> >  >();
}

void InvocationRequestEvent::writeObject (ObjectOutputStream& out) const
{
    presents::dobj::DEvent::writeObject(out);
    out.writeInt(invCode);
    out.writeByte(methodId);
    out.writeField(args);
}
