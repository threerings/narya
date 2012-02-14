#pragma once

namespace presents 
{    enum DisconnectReason {
        NETWORK_DISCONNECT,
        SERVER_DISCONNECT,
        CLIENT_CLOSED,
        SERVER_UNREACHABLE,
        UNKNOWN
    };
    
    typedef boost::signal<void (DisconnectReason)> DisconnectEvent;
}