#include "presents/stable.h"
#include "InvocationMarshaller.h"

using namespace presents::data;

DEFINE_STREAMABLE("com.threerings.presents.data.InvocationMarshaller", InvocationMarshaller);

void InvocationMarshaller::readObject (ObjectInputStream& in)
{
    invOid = int32(in.readInt());
    invCode = int32(in.readInt());
}

void InvocationMarshaller::writeObject (ObjectOutputStream& out) const
{
    out.writeInt(invOid);
    out.writeInt(invCode);
}
