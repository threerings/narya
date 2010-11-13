#include "presents/stable.h"
#include "DownstreamMessage.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.DownstreamMessage", DownstreamMessage);

void DownstreamMessage::readObject (ObjectInputStream& in)
{
    messageId = int16(in.readShort());
}

void DownstreamMessage::writeObject (ObjectOutputStream& out) const
{
    out.writeShort(messageId);
}
