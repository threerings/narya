#include "presents/stable.h"
#include "FramingOutputStream.h"

#include "presents/Util.h"

using namespace presents;

FramingOutputStream::FramingOutputStream (OutputStream* base)
    : _base(base)
{}

// Returns bytes written 
size_t FramingOutputStream::write(const uint8* pData, size_t bytesToWrite)
{
    uint8* bytes = (uint8*)pData;
    for (int ii = 0; ii < bytesToWrite; ii++) {
        _frameBuffer.push_back(bytes[ii]);
    }
    return bytesToWrite;
}


void FramingOutputStream::writeFrame()
{
    int32 length = hostToPresents((int32)(_frameBuffer.size() + sizeof(int32)));
    _base->write((uint8*)&length, sizeof(int32));
    _base->write(&_frameBuffer[0], _frameBuffer.size());
    
    _frameBuffer.clear();
}