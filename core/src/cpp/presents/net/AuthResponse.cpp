#include "presents/stable.h"
#include "AuthResponse.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.AuthResponse", AuthResponse);

void AuthResponse::readObject (ObjectInputStream& in)
{
    presents::net::DownstreamMessage::readObject(in);
    data = boost::static_pointer_cast<presents::net::AuthResponseData>(in.readObject());
}

void AuthResponse::writeObject (ObjectOutputStream& out) const
{
    presents::net::DownstreamMessage::writeObject(out);
    out.writeObject(data);
}
