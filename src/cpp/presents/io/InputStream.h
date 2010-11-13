#pragma once

namespace presents {
class InputStream {
public:
    virtual ~InputStream() {};
    virtual size_t read(void* pData, size_t bytesToRead) = 0;
};
}
