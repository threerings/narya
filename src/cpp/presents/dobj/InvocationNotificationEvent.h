#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/dobj/DEvent.h"

namespace presents { namespace dobj { 

class InvocationNotificationEvent : public presents::dobj::DEvent {
public:
    DECLARE_STREAMABLE();

    int16 receiverId;
    int8 methodId;
    Shared< std::vector< Shared<Streamable> > > args;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}