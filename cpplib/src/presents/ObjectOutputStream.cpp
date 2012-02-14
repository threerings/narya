#include "stable.h"

#include "presents/io/OutputStream.h"
#include "ObjectOutputStream.h"

#include "Streamable.h"
#include "Streamer.h"
#include "Util.h"

#pragma warning (disable: 4100) // unreferenced parameter

using namespace presents;

namespace {
template <class T>
static void writeInteger(OutputStream* out, T value)
{
	value = hostToPresents(value);
	out->write((uint8*)&value, sizeof(value));
}
}

void ObjectOutputStream::writeBoolean (bool value)
{
    writeInteger(_out, (uint8)(value ? 1 : 0));
}

void ObjectOutputStream::writeByte (int8 value)
{
    writeInteger(_out, value);
}

void ObjectOutputStream::writeLong (int64 value)
{
    writeInteger(_out, value);
}

void ObjectOutputStream::writeInt (int32 value)
{
    writeInteger(_out, value);
}

void ObjectOutputStream::writeShort (int16 value)
{
    writeInteger(_out, value);
}

void ObjectOutputStream::writeBytes (const uint8* bytes, size_t length)
{
    _out->write(bytes, length);
}

void ObjectOutputStream::writeDouble (double value)
{
    _out->write((uint8*) &value, 8);
}

void ObjectOutputStream::writeFloat (float value)
{
    _out->write((uint8*) &value, 4);
}

void ObjectOutputStream::writeUTF (const utf8& value)
{
    size_t bytesToWrite = value.size() * sizeof(uint8);
	writeShort(bytesToWrite);
    _out->write((uint8*)value.data(), value.size());
}

void ObjectOutputStream::writeBareObject (const Shared<void>& object, Streamer* streamer)
{
    streamer->writeObject(object, *this);
}

void ObjectOutputStream::writeField (const Shared<void>& object, const utf8& javaName)
{
    writeBoolean(object != NULL);
    if (object != NULL) {
        writeBareObject(object, getStreamer(javaName));
    }
}
