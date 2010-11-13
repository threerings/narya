//
// $Id$

#pragma once

#include <string>
#include <map>
#include <vector>
#include <boost/format.hpp>

#include "ClassMapping.h"
#include "PresentsError.h"
#include "Streamable.h"
#include "Streamer.h"

//interface Streamable;
//interface Streamer;

namespace presents { class OutputStream; }

using namespace presents;

class ObjectOutputStream
{
public:
    ObjectOutputStream (presents::OutputStream* stream) : _out(stream), _nextClassCode(1) { }

    void writeBoolean (bool value);
    void writeByte (int8 value);
    void writeShort (int16 value);
    void writeInt (int32 value);
    void writeLong (int64 value);
    void writeBytes (const uint8* bytes, size_t length);
    void writeDouble (double value);
    void writeFloat (float value);
    void writeUTF (const utf8& value);
    
    template <typename T>
    void writeObject (const Shared<T>& object)
    {
        if (object.get() == NULL) {
            writeShort(0);
            return;
        }
        
        const utf8 className = getObjectName(object.get());
        ClassMap::iterator it = _classMap.find(className);
        Streamer* streamer;
        
        if (it == _classMap.end()) {
            streamer = getStreamer(className);
            if (streamer == NULL) {
                throw PresentsError((boost::format("No streamer for '%1%'") % className).str());
            }
            
            ClassMapping cmap = { (int16)_nextClassCode++, streamer };
            assert(_nextClassCode < 32767); // Can't be more than Short.MAX_VALUE
            _classMap[className] = cmap;
                        
            writeShort(-cmap.code);
            writeUTF(className);
            
        } else {
            ClassMapping* cmap = &(it->second);
            streamer = cmap->streamer;
            writeShort(cmap->code);
        }
        
        writeBareObject((Shared<void>)object, streamer);
    }
    
    template <typename T>
    void writeField (const Shared<T>& object)
    {
        writeField(object, getJavaName((T*)NULL));
    }

    void writeField (const Shared<void>& object, const utf8& javaName);

protected:
    void writeBareObject (const Shared<void>& object, Streamer* streamer);

    presents::OutputStream* _out;

    typedef std::map<utf8, ClassMapping> ClassMap;
    ClassMap _classMap;
    
    int _nextClassCode;
};
