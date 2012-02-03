//
// $Id$

#pragma once

#include "ClassMapping.h"
#include "Streamer.h"

namespace presents { class InputStream; }

using namespace presents;

class ObjectInputStream
{
public:
    ObjectInputStream (presents::InputStream* stream) : _in(stream), _classMap(1) { }
    
    bool readBoolean ();
    int8 readByte ();
    int16 readShort ();
    int32 readInt ();
    int64 readLong ();
    void readBytes (uint8* bytes, size_t length);
    float readFloat ();
    double readDouble ();
    utf8 readUTF ();

    Shared<void> readObject ();
    
    template <typename T>
    Shared<T> readField ()
    {
        return boost::static_pointer_cast<T>(readField(getJavaName((const T*)NULL)));
    }

    Shared<void> readField (utf8 javaname)
    {
        if (readBoolean()) {
            return getStreamer(javaname)->createObject(*this);
        } else {
            return Shared<void>();
        }
    }

protected:
    presents::InputStream* _in;
    typedef std::vector<ClassMapping> ClassMap;
    ClassMap _classMap;
};
