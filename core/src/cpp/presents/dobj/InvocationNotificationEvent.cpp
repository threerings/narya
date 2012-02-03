#include "presents/stable.h"
#include "InvocationNotificationEvent.h"

using namespace presents::dobj;

DEFINE_STREAMABLE("com.threerings.presents.dobj.InvocationNotificationEvent", InvocationNotificationEvent);

void InvocationNotificationEvent::readObject (ObjectInputStream& in)
{
    presents::dobj::DEvent::readObject(in);
    receiverId = int16(in.readShort());
    methodId = int8(in.readByte());
    args = in.readField<  std::vector< Shared<Streamable> >  >();
}

void InvocationNotificationEvent::writeObject (ObjectOutputStream& out) const
{
    presents::dobj::DEvent::writeObject(out);
    out.writeShort(receiverId);
    out.writeByte(methodId);
    out.writeField(args);
}
