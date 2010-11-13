#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/net/DownstreamMessage.h"

namespace presents { namespace net { 

class PongResponse : public presents::net::DownstreamMessage {
public:
    DECLARE_STREAMABLE();

    int64 packStamp;
    int32 processDelay;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}