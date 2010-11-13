#pragma once

#include "presents/Streamer.h"
#include "presents/Streamable.h"

template <typename T>
class StreamableStreamer : public Streamer
{
public:
    Shared<void> createObject (ObjectInputStream& in)
    {
        Shared<Streamable> streamable(new T());
        streamable->readObject(in);
        return streamable;
    }

    void writeObject (const Shared<void>& object, ObjectOutputStream& out)
    {
        ((Shared<T>&)object)->writeObject(out);
    }
};

#define DECLARE_STREAMABLE() \
    static const utf8& javaName (); \
    static void registerWithPresents () { javaName(); } \
    virtual const utf8& getJavaClassName () const { return javaName(); }

#define DEFINE_STREAMABLE(nameString, StreamableClass) \
    static const utf8& gStreamableClassRegistration = StreamableClass::javaName(); \
    const utf8& StreamableClass::javaName () {\
    static const utf8 JAVA_NAME = nameString; \
    static bool gRegistered = false; \
    if (!gRegistered) { registerStreamer(JAVA_NAME, new StreamableStreamer<StreamableClass>()); gRegistered = true; } \
    return JAVA_NAME; }
