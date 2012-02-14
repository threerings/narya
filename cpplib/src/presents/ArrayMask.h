#pragma once

#include "ObjectInputStream.h"
#include "ObjectOutputStream.h"

class ArrayMask
{
public:
    ArrayMask (int length);
    ArrayMask (ObjectInputStream& in);
    ~ArrayMask();
    void set (int index);
    bool isSet (int index);
    void writeTo (ObjectOutputStream& out);
protected:
    uint8* _mask;
    int _length;
};
