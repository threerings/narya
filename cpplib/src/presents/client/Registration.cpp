#include "presents/stable.h"
#include "Registration.h"

using namespace presents::client;

DEFINE_STREAMABLE("com.threerings.presents.client.InvocationReceiver$Registration", Registration);

void Registration::readObject (ObjectInputStream& in)
{
    receiverCode = in.readField< utf8 >();
    receiverId = int16(in.readShort());
}

void Registration::writeObject (ObjectOutputStream& out) const
{
    out.writeField(receiverCode);
    out.writeShort(receiverId);
}
