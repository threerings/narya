#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/dobj/DEvent.h"
#include "presents/net/DownstreamMessage.h"

namespace presents { namespace net { 

class EventNotification : public presents::net::DownstreamMessage {
public:
    DECLARE_STREAMABLE();

    Shared<presents::dobj::DEvent> event;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}