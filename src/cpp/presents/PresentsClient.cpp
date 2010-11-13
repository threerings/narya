//
// $Id$

#include "presents/stable.h"
#include "PresentsClient.h"

#include "SocketStream.h"
#include "ObjectInputStream.h"
#include "ObjectOutputStream.h"

#include "presents/box/BoxedBoolean.h"
#include "presents/box/BoxedByte.h"
#include "presents/box/BoxedDouble.h"
#include "presents/box/BoxedFloat.h"
#include "presents/box/BoxedInt.h"
#include "presents/box/BoxedShort.h"

#include "presents/client/Registration.h"

#include "presents/data/ClientObject.h"
#include "presents/data/InvocationMarshaller.h"
#include "presents/data/PermissionPolicy.h"
#include "presents/data/RegistrationMarshaller.h"

#include "presents/dobj/DEvent.h"
#include "presents/dobj/DObject.h"
#include "presents/dobj/DSet.h"
#include "presents/dobj/EntryAddedEvent.h"
#include "presents/dobj/EntryRemovedEvent.h"
#include "presents/dobj/InvocationNotificationEvent.h"
#include "presents/dobj/InvocationRequestEvent.h"
#include "presents/dobj/NamedEvent.h"

#include "presents/io/FramedInputStream.h"
#include "presents/io/FramingOutputStream.h"

#include "presents/net/AuthRequest.h"
#include "presents/net/AuthResponse.h"
#include "presents/net/AuthResponseData.h"
#include "presents/net/BootstrapData.h"
#include "presents/net/BootstrapNotification.h"
#include "presents/net/Credentials.h"
#include "presents/net/DownstreamMessage.h"
#include "presents/net/EventNotification.h"
#include "presents/net/ForwardEventRequest.h"
#include "presents/net/ObjectResponse.h"
#include "presents/net/PingRequest.h"
#include "presents/net/PongResponse.h"
#include "presents/net/UpstreamMessage.h"
#include "presents/net/UsernamePasswordCreds.h"

#include "util/Name.h"

#include "presents/Util.h"

using namespace presents;

PresentsClient::PresentsClient ()
    : _outMessageId(0)
    , _nextReceiverId(1)
    , _oid(-1)
    , _socket(NULL)
    , _idleTime(0)
    , _connected(false)
    , _active(false)
{
    // ensure all Streamables are not stripped out of the library by clients
    // (Some of these classes aren't directly referenced by PresentsClient)
    box::BoxedBoolean::registerWithPresents();
    box::BoxedByte::registerWithPresents();
    box::BoxedDouble::registerWithPresents();
    box::BoxedFloat::registerWithPresents();
    box::BoxedInt::registerWithPresents();
    box::BoxedShort::registerWithPresents();
    
    client::Registration::registerWithPresents();
    
    data::ClientObject::registerWithPresents();
    data::InvocationMarshaller::registerWithPresents();
    data::PermissionPolicy::registerWithPresents();
    data::RegistrationMarshaller::registerWithPresents();
    
    dobj::DEvent::registerWithPresents();
    dobj::DObject::registerWithPresents();
    dobj::DSet::registerWithPresents();
    dobj::EntryAddedEvent::registerWithPresents();
    dobj::EntryRemovedEvent::registerWithPresents();
    dobj::InvocationNotificationEvent::registerWithPresents();
    dobj::InvocationRequestEvent::registerWithPresents();
    dobj::NamedEvent::registerWithPresents();
    
    net::AuthRequest::registerWithPresents();
    net::AuthResponse::registerWithPresents();
    net::AuthResponseData::registerWithPresents();
    net::BootstrapData::registerWithPresents();
    net::BootstrapNotification::registerWithPresents();
    net::Credentials::registerWithPresents();
    net::DownstreamMessage::registerWithPresents();
    net::EventNotification::registerWithPresents();
    net::ForwardEventRequest::registerWithPresents();
    net::ObjectResponse::registerWithPresents();
    net::PingRequest::registerWithPresents();
    net::PongResponse::registerWithPresents();
    net::UpstreamMessage::registerWithPresents();
    net::UsernamePasswordCreds::registerWithPresents();
}

PresentsClient::~PresentsClient ()
{
}

bool PresentsClient::isConnected () const
{
    return _connected;
}

void PresentsClient::connect (const std::string& host, int port, Shared<presents::net::Credentials> creds, Shared<utf8> version)
{
    assert(!_active);
    assert(!_onSocketDisconnect.connected());
    _active = true;
    _socket.reset(createSocketStream(host.c_str(), port));
    _onSocketDisconnect = _socket->onDisconnect(boost::bind(&PresentsClient::handleDisconnect, this, _1));
    _framedIn.reset(new FramedInputStream(_socket.get()));
    _framingOut.reset(new FramingOutputStream(_socket.get()));
    _in.reset(new ObjectInputStream(_framedIn.get()));
    _out.reset(new ObjectOutputStream(_framingOut.get()));
    
    Shared<net::AuthRequest> req(new net::AuthRequest);
    req->version = version;
    req->zone = Shared<utf8>(new utf8(""));

    req->creds = creds;
    req->bootGroups = Shared< std::vector< Shared<utf8> > >(new std::vector< Shared<utf8> >);
    req->bootGroups->push_back(Shared<utf8>(new utf8("client")));
    PLOG("Connecting to %s:%d", host.c_str(), port);
    postMessage(req);
}

void PresentsClient::disconnect ()
{
    if (_active) {
        _socket->close();
    }
}

void PresentsClient::update (float dt)
{
    if (!_active) {
        return;
    }
    _socket->update(dt);
    if (!_active) {
        // Update can inform of uf a disconnect through onDisconnect, which shuts us down.  
        return;
    }
    while (_framedIn->readFrame()) {
        Shared<Streamable> obj = boost::static_pointer_cast<Streamable>(_in->readObject());
        if (obj == NULL) {
            PLOG("Got null message? What the dickens?");
            continue;
        }
        const utf8& msgName = obj->getJavaClassName();
        if (msgName == net::AuthResponse::javaName()) {
            Shared<net::AuthResponse> resp = boost::static_pointer_cast<net::AuthResponse>(obj);
            PLOG("Got auth response from corpseserver: %s", resp->data->code->c_str());
            _onAuth(*resp->data);
        } else if (msgName == net::BootstrapNotification::javaName()) {
            PLOG("Got bootstrap");
            Shared<net::BootstrapNotification> strap = boost::static_pointer_cast<net::BootstrapNotification>(obj);
            _oid = strap->data->clientOid;
            typedef std::vector< Shared<data::InvocationMarshaller> > MarshVec;
            Shared<MarshVec> services(strap->data->services);
            for (MarshVec::iterator iter = services->begin(); iter != services->end(); ++iter) {
                _services.insert(MarshallerMap::value_type((*iter)->getJavaClassName(), *iter));
            }
            _connected = true;
            _onConnected(this);
        } else if (msgName == net::EventNotification::javaName()) {
            Shared<dobj::DEvent> event = boost::static_pointer_cast<net::EventNotification>(obj)->event;
            if (event->getJavaClassName() == dobj::InvocationNotificationEvent::javaName()) {
                Shared<dobj::InvocationNotificationEvent> notification = boost::static_pointer_cast<dobj::InvocationNotificationEvent>(event);
                DecoderMap::iterator it = _decoders.find(notification->receiverId);
                it->second->dispatchNotification(notification->methodId, *notification->args);
            } else {
                PLOG("Ignoring event %s", event->getJavaClassName().c_str());
            }
        } else {
            PLOG("Ignoring message %s", msgName.c_str());
        }
        if (!_active) {
            // The client may decide to disconnect as a result of reading the message, in which case we need to stop reading.
            return;
        }
    }
    _idleTime += dt;
    if (!_connected) {
        if (_idleTime > 30) {
            handleDisconnect(SERVER_UNREACHABLE);
        }
    } else if (_idleTime > 60) {
        Shared<net::PingRequest> ping(new net::PingRequest());
        postMessage(ping);
    }
}

void PresentsClient::postMessage (Shared<net::UpstreamMessage> message)
{
    if (!_active) {
        PLOG("PresentsClient::postMessage called while not connected!");
    } else {
        message->messageId = _outMessageId++;
        _out->writeObject(message);
        _framingOut->writeFrame();
        _idleTime = 0;
    }
}

void PresentsClient::sendRequest (int32 invOid, int32 invCode, int8 methId, const Shared< std::vector< Shared<Streamable> > >& args)
{
    Shared<dobj::InvocationRequestEvent> req(new dobj::InvocationRequestEvent);
    req->toid = invOid;
    req->invCode = invCode;
    req->methodId = methId;
    req->args = args;
    Shared<net::ForwardEventRequest> forward(new net::ForwardEventRequest);
    forward->event = req;
    postMessage(forward);
}


void PresentsClient::handleDisconnect (DisconnectReason reason)
{
    PLOG("PresentsClient::handleDisconnect");
    _active = false;
    _socket.reset();
    _in.reset();
    _out.reset();
    _framedIn.reset();
    _framingOut.reset();
    _oid = -1;
    _decoders.clear();
    _services.clear();
    _onSocketDisconnect.disconnect();
    _connected = false;
    _onDisconnected(reason);
}

Shared<PresentsClient> PresentsClient::getSharedThis()
{
    return shared_from_this();
}
