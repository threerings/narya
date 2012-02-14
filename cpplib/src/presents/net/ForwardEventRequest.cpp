#include "presents/stable.h"
#include "ForwardEventRequest.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.ForwardEventRequest", ForwardEventRequest);

void ForwardEventRequest::readObject (ObjectInputStream& in)
{
    presents::net::UpstreamMessage::readObject(in);
    event = boost::static_pointer_cast<presents::dobj::DEvent>(in.readObject());
}

void ForwardEventRequest::writeObject (ObjectOutputStream& out) const
{
    presents::net::UpstreamMessage::writeObject(out);
    out.writeObject(event);
}
