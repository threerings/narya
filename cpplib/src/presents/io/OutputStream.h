#pragma once

namespace presents {
class OutputStream {
public:
    virtual ~OutputStream() {};
    virtual size_t write(const uint8* pData, size_t bytesToWrite) = 0;
};
}
