#pragma once

#include "presents/Streamable.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
#include "presents/data/InvocationMarshaller.h"
#include "presents/streamers/StreamableStreamer.h"
#include <boost/enable_shared_from_this.hpp>
#include "presents/client/Registration.h"

namespace presents { namespace data { 

class RegistrationMarshaller : public presents::data::InvocationMarshaller, public boost::enable_shared_from_this<RegistrationMarshaller> {
public:
    DECLARE_STREAMABLE();

    virtual ~RegistrationMarshaller () {}

    void registerReceiver (Shared<presents::PresentsClient> client, Shared<presents::client::Registration> arg1);
protected:
    Shared<RegistrationMarshaller> getSharedThis();
};

}}