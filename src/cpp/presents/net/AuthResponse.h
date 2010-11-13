#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/net/AuthResponseData.h"
#include "presents/net/DownstreamMessage.h"

namespace presents { namespace net { 

class AuthResponse : public presents::net::DownstreamMessage {
public:
    DECLARE_STREAMABLE();

    Shared<presents::net::AuthResponseData> data;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}