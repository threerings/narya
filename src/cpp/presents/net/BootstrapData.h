#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/data/InvocationMarshaller.h"
#include "presents/Streamable.h"

namespace presents { namespace net { 

class BootstrapData : public Streamable {
public:
    DECLARE_STREAMABLE();

    int32 connectionId;
    int32 clientOid;
    Shared< std::vector< Shared<presents::data::InvocationMarshaller> > > services;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}