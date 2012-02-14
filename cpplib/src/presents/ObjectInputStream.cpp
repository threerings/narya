#include "stable.h"

#include <boost/format.hpp>

#include "ObjectInputStream.h"
#include "PresentsError.h"
#include "presents/io/InputStream.h"

#include "Streamer.h"

#include "Util.h"
bool ObjectInputStream::readBoolean ()
{
    return readByte() != 0;
}

namespace
{
    template <class T>
    T readInteger(presents::InputStream* in)
    {
        T value;
        assert(in->read(&value, sizeof(T)) == sizeof(T));
        return presents::presentsToHost(value);
    }
}

int8 ObjectInputStream::readByte ()
{
    return readInteger<int8>(_in);
}

int16 ObjectInputStream::readShort ()
{
    return readInteger<int16>(_in);
}

int32 ObjectInputStream::readInt ()
{
    return readInteger<int32>(_in);
}

int64 ObjectInputStream::readLong ()
{
    return readInteger<int64>(_in);
}

void ObjectInputStream::readBytes (uint8* bytes, size_t length)
{
    _in->read(bytes, length);
}

float ObjectInputStream::readFloat ()
{
    float value;
    readBytes((uint8*) &value, 4);
    return value;
}

double ObjectInputStream::readDouble ()
{
    double value;
    readBytes((uint8*) &value, 8);
    return value;
}

utf8 ObjectInputStream::readUTF ()
{
    uint16 dataLength = readInteger<uint16>(_in);
    boost::scoped_array<uint8> data(new uint8[dataLength]);
    readBytes((uint8*)&data[0], dataLength);
    return std::string((char*)&data[0], dataLength);
}

Shared<void> ObjectInputStream::readObject ()
{
    int16 code = readShort();
    if (code == 0) {
        return Shared<void>();
    }

    Streamer* streamer;
    if (code < 0) {
        code *= -1;

        utf8 name = readUTF();
        
        streamer = getStreamer(name);
        ClassMapping cmap = { code, streamer };
        _classMap.insert(_classMap.begin() + code, cmap);
        
    } else {
        streamer = (&_classMap[code])->streamer;
        if (streamer == NULL) {
            throw PresentsError((boost::format("Unknown streamer code '%1%'") % code).str());
        }
    }
    
    return streamer->createObject(*this);
}