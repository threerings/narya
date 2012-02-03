#include "presents/stable.h"
#include "UpstreamMessage.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.UpstreamMessage", UpstreamMessage);

void UpstreamMessage::readObject (ObjectInputStream& in)
{
    messageId = int16(in.readShort());
}

void UpstreamMessage::writeObject (ObjectOutputStream& out) const
{
    out.writeShort(messageId);
}
