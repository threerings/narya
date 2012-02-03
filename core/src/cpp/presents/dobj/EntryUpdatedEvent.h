#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/Streamable.h"
#include "presents/dobj/EntryEvent.h"

namespace presents { namespace dobj { 

class EntryUpdatedEvent : public presents::dobj::EntryEvent {
public:
    DECLARE_STREAMABLE();

    Shared<Streamable> entry;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}