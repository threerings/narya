#pragma once

#include "InputStream.h"

namespace presents {
    
class FramedInputStream : public InputStream
{
public:
    FramedInputStream (InputStream* base);
    virtual size_t read(void* pData, size_t bytesToRead);
    bool readFrame ();
protected:
    int32 _length;
    InputStream* _base;
    boost::scoped_array<uint8> _readBuffer;
    int32 _fillPosition, _readPosition, _readBufferLength;
};
}