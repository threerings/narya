#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/dobj/DEvent.h"

namespace presents { namespace dobj { 

class NamedEvent : public presents::dobj::DEvent {
public:
    DECLARE_STREAMABLE();

    Shared<utf8> name;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}