#include "presents/stable.h"
#include "EventNotification.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.EventNotification", EventNotification);

void EventNotification::readObject (ObjectInputStream& in)
{
    presents::net::DownstreamMessage::readObject(in);
    event = boost::static_pointer_cast<presents::dobj::DEvent>(in.readObject());
}

void EventNotification::writeObject (ObjectOutputStream& out) const
{
    presents::net::DownstreamMessage::writeObject(out);
    out.writeObject(event);
}
