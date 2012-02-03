#pragma once

#include "Streamable.h"

namespace presents
{
    class InvocationDecoder {
    public:
        const utf8 receiverCode; 
        InvocationDecoder(utf8 receiverCode);
        virtual void dispatchNotification(int8 methodId, const std::vector< Shared<Streamable> >& args) = 0;

    private:
        // not implemented
        InvocationDecoder (const InvocationDecoder&);
        InvocationDecoder& operator= (const InvocationDecoder&);
    };
}