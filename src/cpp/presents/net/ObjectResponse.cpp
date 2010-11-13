#include "presents/stable.h"
#include "ObjectResponse.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.ObjectResponse", ObjectResponse);

void ObjectResponse::readObject (ObjectInputStream& in)
{
    presents::net::DownstreamMessage::readObject(in);
    dobj = boost::static_pointer_cast<Streamable>(in.readObject());
}

void ObjectResponse::writeObject (ObjectOutputStream& out) const
{
    presents::net::DownstreamMessage::writeObject(out);
    out.writeObject(dobj);
}
