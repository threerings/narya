#pragma once

#include "OutputStream.h"

namespace presents {
    
class FramingOutputStream : public OutputStream {
public:
    FramingOutputStream (OutputStream* base);
    
    // Returns bytes written 
    virtual size_t write (const uint8* pData, size_t bytesToWrite);
    void writeFrame ();

protected:
    OutputStream* _base;
    std::vector<uint8> _frameBuffer;
};
}