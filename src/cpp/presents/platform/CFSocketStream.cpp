//
// $Id$

#include "presents/stable.h"
#include "presents/SocketStream.h"
#include "presents/Util.h"
#include <CoreFoundation/CoreFoundation.h>
#include <CFNetwork/CFNetwork.h>

namespace presents
{
    
class CFSocketStream : public SocketStream
{
public:
    CFSocketStream (const CFStringRef hostname, int port)
        : _reader(NULL)
        , _writer(NULL)
    {
        CFStreamCreatePairWithSocketToHost(kCFAllocatorDefault, hostname, port, &_reader, &_writer);
        CFReadStreamOpen(_reader);
        CFWriteStreamOpen(_writer);
    }
    
    virtual ~CFSocketStream ()
    {
        CloseWithReason(CLIENT_CLOSED, "Destroyed!");
    }

    // Returns bytes read
    virtual size_t read (void* pData, size_t bytesToRead)
    {
        if (_reader == NULL) {
            PLOG("Attempted to read from closed stream!");
            return 0;
        }
        switch (CFReadStreamGetStatus(_reader)) {
            case kCFStreamStatusClosed:
                CloseWithReason(SERVER_DISCONNECT, "Server closed our read connection");
                return 0;
            case kCFStreamStatusError:
                HandleStreamError(CFReadStreamCopyError(_reader));
                return 0;
        }
        if (CFReadStreamHasBytesAvailable(_reader)) {
            int read = CFReadStreamRead(_reader, (UInt8*)pData, bytesToRead);
            if (read < 0) {
                PLOG("Got error while reading!");
                HandleStreamError(CFReadStreamCopyError(_reader));
                return 0;
            } else if (read == 0 && CFReadStreamGetStatus(_reader) == kCFStreamStatusAtEnd) {
                // Only disconnect due to end-of-stream if the stream thinks there should be data
                CloseWithReason(SERVER_DISCONNECT, "Hit read stream end");
            }
            return read;
        }
        return 0;
    }

    // Returns bytes written
    virtual size_t write (const uint8* pData, size_t bytesToWrite)
    {
        if (_writer == NULL) {
            PLOG("Attempted to write to closed stream!");
            return 0;
        }
        uint8* bytes = (uint8*)pData;
        for (int ii = 0; ii < bytesToWrite; ii++) {
            _writerBuffer.push_back(bytes[ii]);
        }
        return bytesToWrite;
    }

    virtual void update (float dt)
    {
        if (_writer == NULL) {
            PLOG("Attempted to write to closed stream!");
            return;
        }
        switch (CFWriteStreamGetStatus(_writer)) {
            case kCFStreamStatusClosed:
                CloseWithReason(SERVER_DISCONNECT, "Server closed our write connection");
                return;
            case kCFStreamStatusAtEnd:
                CloseWithReason(SERVER_DISCONNECT, "Hit write stream end");
                return;
            case kCFStreamStatusError:
                HandleStreamError(CFWriteStreamCopyError(_writer));
                return;
        }
        while (!_writerBuffer.empty() && CFWriteStreamCanAcceptBytes(_writer)) {
            int written = CFWriteStreamWrite(_writer, (UInt8*)&_writerBuffer[0], _writerBuffer.size());
            if (written < 0) {
                PLOG("Hit error in write!");
                HandleStreamError(CFWriteStreamCopyError(_writer));
            } else {
                _writerBuffer.erase(_writerBuffer.begin(), _writerBuffer.begin() + written);
            }
        }
    }
    
    virtual void close ()
    {
        CloseWithReason(CLIENT_CLOSED, "Client requested close");
    }
    
    virtual connection_t onDisconnect (const DisconnectEvent::slot_type& slot)  
    {
        return _onDisconnected.connect(slot);
    }
    
protected:
    void CloseWithReason (presents::DisconnectReason why, const std::string& detail)
    {            
        if (_reader == NULL) {
            return;// Multiple closes don't hurt nobody
        }
        PLOG("Closing stream: %s", detail.c_str());
        CFReadStreamClose(_reader);
        CFRelease(_reader);
        _reader = NULL;
        CFWriteStreamClose(_writer);
        CFRelease(_writer);
        _writer = NULL;
        PLOG("Signaling disconnected");
        _onDisconnected(why);
    }
    
    void HandleStreamError (CFErrorRef error)
    {
        if (CFErrorGetDomain(error) == kCFErrorDomainPOSIX) {
            switch (CFErrorGetCode(error)) {
                case ENETDOWN:
                    CloseWithReason(NETWORK_DISCONNECT, "Net down!");
                    break;
                case ENETUNREACH:
                    CloseWithReason(NETWORK_DISCONNECT, "Net unreachable");
                    break;
                case ENETRESET:
                    CloseWithReason(NETWORK_DISCONNECT, "Net reset");
                    break;
                case ECONNRESET:
                    CloseWithReason(NETWORK_DISCONNECT, "Net connection reset");
                    break;
                case EHOSTDOWN:
                    CloseWithReason(SERVER_UNREACHABLE, "host down!");
                    break;
                case EHOSTUNREACH:
                    CloseWithReason(SERVER_UNREACHABLE, "host unreachable!");
                    break;
                case ECONNREFUSED:
                    CloseWithReason(SERVER_UNREACHABLE, "connection refused");
                    break;
                case EPIPE:
                    CloseWithReason(SERVER_DISCONNECT, "broken pipe");
                    break;
                case ENOTCONN:
                    CloseWithReason(SERVER_DISCONNECT, "not connected");
                    break;
                default:
                    PLOG("Unknown posix net error: %d", CFErrorGetCode(error));
                    CloseWithReason(NETWORK_DISCONNECT, "Unknown failure!");
                    break;
            }
        } else {
            PLOG("Unknown CFStreamError domain: domain: %s error: %d", CFErrorGetDomain(error), CFErrorGetCode(error));
            CloseWithReason(UNKNOWN, "CFStream error");
        }
        CFRelease(error);
    }
    
    CFWriteStreamRef _writer;
    std::vector<uint8> _writerBuffer;
    CFReadStreamRef _reader;
    DisconnectEvent _onDisconnected;
};

SocketStream* createSocketStream (const char* hostname, int port)
{
    CFStringRef cfHostname = CFStringCreateWithCString(kCFAllocatorDefault, hostname,
                                                       kCFStringEncodingMacRoman);
    CFSocketStream* stream = new CFSocketStream(cfHostname, port);
    CFRelease(cfHostname);
    return stream;
}
    
}
