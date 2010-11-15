#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/data/PermissionPolicy.h"
#include "presents/dobj/DObject.h"
#include "presents/dobj/DSet.h"
#include "util/Name.h"

namespace presents { namespace data { 

class ClientObject : public presents::dobj::DObject {
public:
    DECLARE_STREAMABLE();

    Shared<util::Name> username;
    Shared<presents::dobj::DSet> receivers;
    Shared<presents::data::PermissionPolicy> permPolicy;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}