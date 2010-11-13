#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/dobj/NamedEvent.h"
#include "presents/Streamable.h"

namespace presents { namespace dobj { 

class EntryAddedEvent : public presents::dobj::NamedEvent {
public:
    DECLARE_STREAMABLE();

    Shared<Streamable> entry;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}