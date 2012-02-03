#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/Streamable.h"

namespace presents { 

class PresentsClient;
    
namespace data { 

class InvocationMarshaller : public  Streamable  {
public:
    DECLARE_STREAMABLE();

    int32 invOid;
    int32 invCode;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}