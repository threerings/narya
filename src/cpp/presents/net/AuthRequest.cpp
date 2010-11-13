#include "presents/stable.h"
#include "AuthRequest.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.AuthRequest", AuthRequest);

void AuthRequest::readObject (ObjectInputStream& in)
{
    presents::net::UpstreamMessage::readObject(in);
    creds = boost::static_pointer_cast<presents::net::Credentials>(in.readObject());
    version = in.readField< utf8 >();
    zone = in.readField< utf8 >();
    bootGroups = boost::static_pointer_cast< std::vector< Shared<utf8> > >(in.readObject());
}

void AuthRequest::writeObject (ObjectOutputStream& out) const
{
    presents::net::UpstreamMessage::writeObject(out);
    out.writeObject(creds);
    out.writeField(version);
    out.writeField(zone);
    out.writeObject(bootGroups);
}
