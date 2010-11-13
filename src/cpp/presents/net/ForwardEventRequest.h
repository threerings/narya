#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/net/UpstreamMessage.h"
#include "presents/dobj/DEvent.h"

namespace presents { namespace net { 

class ForwardEventRequest : public presents::net::UpstreamMessage {
public:
    DECLARE_STREAMABLE();

    Shared<presents::dobj::DEvent> event;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}