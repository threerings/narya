#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/net/UpstreamMessage.h"

namespace presents { namespace net { 

class PingRequest : public presents::net::UpstreamMessage {
public:
    DECLARE_STREAMABLE();


    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}