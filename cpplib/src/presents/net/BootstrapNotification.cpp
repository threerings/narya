#include "presents/stable.h"
#include "BootstrapNotification.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.BootstrapNotification", BootstrapNotification);

void BootstrapNotification::readObject (ObjectInputStream& in)
{
    presents::net::DownstreamMessage::readObject(in);
    data = boost::static_pointer_cast<presents::net::BootstrapData>(in.readObject());
}

void BootstrapNotification::writeObject (ObjectOutputStream& out) const
{
    presents::net::DownstreamMessage::writeObject(out);
    out.writeObject(data);
}
