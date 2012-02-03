#include "stable.h"
#include "ArrayMask.h"

ArrayMask::ArrayMask (int length)
{
    _length = length/8;
    if (length % 8 != 0) {
        _length++;
    }
    _mask = new uint8[_length];
}

ArrayMask::ArrayMask (ObjectInputStream& in)
{
    _length = in.readShort();
    _mask = new uint8[_length];
    in.readBytes(_mask, _length);
}

ArrayMask::~ArrayMask()
{
    delete _mask;
}

void ArrayMask::set (int index)
{
    _mask[index/8] |= (1 << (index%8));
}

bool ArrayMask::isSet (int index)
{
    return (_mask[index/8] & (1 << (index%8))) != 0;
}

void ArrayMask::writeTo (ObjectOutputStream& out)
{
    out.writeShort((int16) _length);
    out.writeBytes(_mask, _length);
}