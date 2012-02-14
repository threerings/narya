#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/dobj/DEvent.h"

namespace presents { namespace dobj { 

class InvocationRequestEvent : public presents::dobj::DEvent {
public:
    DECLARE_STREAMABLE();

    int32 invCode;
    int8 methodId;
    Shared< std::vector< Shared<Streamable> > > args;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}