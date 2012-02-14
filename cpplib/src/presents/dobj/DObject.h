#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/Streamable.h"

namespace presents { namespace dobj { 

class DObject : public Streamable {
public:
    DECLARE_STREAMABLE();

    int32 oid;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}