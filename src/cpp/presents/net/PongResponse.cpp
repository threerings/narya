#include "presents/stable.h"
#include "PongResponse.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.PongResponse", PongResponse);

void PongResponse::readObject (ObjectInputStream& in)
{
    presents::net::DownstreamMessage::readObject(in);
    packStamp = int64(in.readLong());
    processDelay = int32(in.readInt());
}

void PongResponse::writeObject (ObjectOutputStream& out) const
{
    presents::net::DownstreamMessage::writeObject(out);
    out.writeLong(packStamp);
    out.writeInt(processDelay);
}
