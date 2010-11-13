#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/net/UpstreamMessage.h"
#include "presents/net/Credentials.h"

namespace presents { namespace net { 

class AuthRequest : public presents::net::UpstreamMessage {
public:
    DECLARE_STREAMABLE();

    Shared<presents::net::Credentials> creds;
    Shared<utf8> version;
    Shared<utf8> zone;
    Shared< std::vector< Shared<utf8> > > bootGroups;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}