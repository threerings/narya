#pragma once

#include "DisconnectReason.h"
#include "io/InputStream.h"
#include "io/OutputStream.h"

namespace presents {
class SocketStream : public InputStream, public OutputStream
{
public:
    virtual ~SocketStream() {};
    virtual void close () = 0;
    virtual void update (float dt) = 0;
    virtual connection_t onDisconnect (const DisconnectEvent::slot_type& slot) = 0;
};

SocketStream* createSocketStream (const char* hostname, int port);
}