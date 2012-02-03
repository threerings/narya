#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/streamers/StreamableStreamer.h"

#include "presents/dobj/DObject.h"

namespace presents { namespace net { 

class AuthResponseData : public presents::dobj::DObject {
public:
    DECLARE_STREAMABLE();

    Shared<utf8> code;

    virtual void readObject(ObjectInputStream& in);
    virtual void writeObject(ObjectOutputStream& out) const;
};

}}