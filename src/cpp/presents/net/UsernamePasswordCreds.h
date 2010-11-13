#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "util/Name.h"
#include "presents/net/Credentials.h"

namespace presents { namespace net { 

class UsernamePasswordCreds : public presents::net::Credentials {
public:
    DECLARE_STREAMABLE();

    Shared<util::Name> username;
    Shared<utf8> password;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}