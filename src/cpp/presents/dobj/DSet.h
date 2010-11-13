#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

namespace presents { namespace dobj { 

class DSet : public  Streamable  {
public:
    DECLARE_STREAMABLE();
    
    Shared< std::vector< Shared<Streamable> > > entries;
    int32 size;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}