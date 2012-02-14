//
// $Id$

#pragma once

#include <boost/format.hpp>

#include "presents/InvocationDecoder.h"
#include "presents/DisconnectReason.h"
#include "presents/PresentsError.h"
#include "presents/client/Registration.h"
#include "presents/data/RegistrationMarshaller.h"

class ObjectInputStream;
class ObjectOutputStream;

namespace presents
{
namespace data { class InvocationMarshaller; }
namespace net { 
    class AuthResponseData;
    class Credentials;    
    class UpstreamMessage;
}
class FramedInputStream;
class FramingOutputStream;
class SocketStream;
    
class PresentsClient : public boost::enable_shared_from_this<PresentsClient>
{
protected:
    typedef std::map<utf8, Shared<data::InvocationMarshaller> > MarshallerMap;
    typedef std::map<int16, Shared<InvocationDecoder> > DecoderMap;
    
public:
    typedef boost::signal<void (PresentsClient*)> ClientEvent;
    typedef boost::signal<void (const net::AuthResponseData&)> AuthFailureEvent;
    
    PresentsClient ();
    virtual ~PresentsClient ();
    
    void update (float dt);

    template <typename T>
    Shared<T> requireService ()
    {
        MarshallerMap::iterator it = _services.find(T::javaName());
        if (it->second == NULL) {
            throw PresentsError((boost::format("No marshaller for %1%") % T::javaName()).str());
        }
        return boost::static_pointer_cast<T>(it->second);
    }

    template <typename T>
    void registerReceiver (Shared<T> receiver)
    {
        assert(_socket != NULL);
        Shared<presents::client::Registration> reg(new presents::client::Registration);
        reg->receiverId = _nextReceiverId++;
        Shared<InvocationDecoder> dec(new typename T::Decoder(receiver));
        reg->receiverCode = Shared<utf8>(new utf8(dec->receiverCode));
        _decoders.insert(DecoderMap::value_type(reg->receiverId, dec));
        requireService<presents::data::RegistrationMarshaller>()->registerReceiver(getSharedThis(), reg);
    }
    
    bool isConnected () const;
    void connect (const std::string& hostname, int port, Shared<presents::net::Credentials> creds, Shared<utf8> version);
    void disconnect ();

    void postMessage (Shared<presents::net::UpstreamMessage> msg);
    void sendRequest (int32 invOid, int32 invCode, int8 methodId, const Shared< std::vector< Shared<Streamable> > >& args);
    int getOid() const { assert(_oid != -1); return _oid; }
    
    connection_t onConnected (const ClientEvent::slot_type& slot) { 
        return _onConnected.connect(slot);
    }
    
    connection_t onDisconnected (const DisconnectEvent::slot_type& slot) { 
        return _onDisconnected.connect(slot);
    }
    
    connection_t onAuth (const AuthFailureEvent::slot_type& slot) { 
        return _onAuth.connect(slot);
    }

protected:
    void handleDisconnect (DisconnectReason);
    Shared<PresentsClient> getSharedThis();
    
    boost::scoped_ptr<SocketStream> _socket;
    boost::scoped_ptr<FramingOutputStream> _framingOut;
    boost::scoped_ptr<FramedInputStream> _framedIn;
    boost::scoped_ptr<ObjectInputStream> _in;
    boost::scoped_ptr<ObjectOutputStream> _out;
    int16 _outMessageId;
    int16 _nextReceiverId;
    int32 _oid;
    DecoderMap _decoders;
    MarshallerMap _services;
    float _idleTime;
    
    // If the connection to the presents server is authenticated and ready to go. _onConnected is
    // signaled when this becomes true
    bool _connected;
    
    // If we're connected or are trying to connect.  This is always true when _connected is true,
    // but also before the connection is authed and before the bootstrap data arrives.
    bool _active;
    
    ClientEvent _onConnected;
    DisconnectEvent _onDisconnected;
    AuthFailureEvent _onAuth;
    
    connection_t _onSocketDisconnect;
};
}
