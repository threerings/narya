#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/Streamable.h"
#include "presents/dobj/EntryEvent.h"

namespace presents { namespace dobj { 

class EntryRemovedEvent : public presents::dobj::EntryEvent {
public:
    DECLARE_STREAMABLE();

    Shared<Streamable> key;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}