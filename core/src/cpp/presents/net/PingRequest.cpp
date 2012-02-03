#include "presents/stable.h"
#include "PingRequest.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.PingRequest", PingRequest);

void PingRequest::readObject (ObjectInputStream& in)
{
    presents::net::UpstreamMessage::readObject(in);
}

void PingRequest::writeObject (ObjectOutputStream& out) const
{
    presents::net::UpstreamMessage::writeObject(out);
}
