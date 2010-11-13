#include "FramedInputStream.h"

#include "presents/stable.h"

#include "presents/PresentsError.h"
#include "presents/Util.h"
#include "FramedInputStream.h"
#include "InputStream.h"

using namespace presents;

static const int READ_BUFFER_LENGTH = 4096;

FramedInputStream::FramedInputStream (InputStream* base)
: _base(base)
, _length(-1)
, _fillPosition(0)
, _readPosition(0)
, _readBufferLength(READ_BUFFER_LENGTH)
, _readBuffer(new uint8[READ_BUFFER_LENGTH])
{
}

// Returns bytes read
size_t FramedInputStream::read (void* pData, size_t bytesToRead)
{
    if (_fillPosition != _length) {
        throw PresentsError("Attempted to read with no frame!");
    }
    if ((int32) (_readPosition + bytesToRead) > _length) {
        throw PresentsError("Attempted to read past frame!");
    }
    memcpy(pData, &_readBuffer[_readPosition], bytesToRead);
    _readPosition += bytesToRead;
    return bytesToRead;
}

bool FramedInputStream::readFrame ()
{
    if (_length == _fillPosition) {
        if (_fillPosition != _readPosition) {
            throw PresentsError("Attempted to read new frame without consuming old one");
        }
        _length = -1;
        _fillPosition = _readPosition = 0;
    }
    if (_length == -1) {
        _fillPosition += _base->read(&_readBuffer[_fillPosition], 4 - _fillPosition);
        if (_fillPosition == 4) {
            _length = ((_readBuffer[0] << 24) | (_readBuffer[1] << 16) | (_readBuffer[2] << 8) | _readBuffer[3]) - 4;
            if (_length > _readBufferLength) {
                _readBufferLength *= 2;
                _readBuffer.reset(new uint8[_readBufferLength]);
                PLOG("Thas a mighty big frame.  Musta been feedin' him corn.  Growing buffer to %d", _readBufferLength);
            }
            _fillPosition = 0;
        } else {
            return false;
        }
    }
    _fillPosition += _base->read(&_readBuffer[_fillPosition], _length - _fillPosition);
    return _fillPosition == _length;
}
