//
// $Id$

#pragma once

#include <boost/type_traits.hpp>
#include "Streamable.h"

static inline utf8 JAVA_LIST_NAME () { return "java.util.ArrayList"; }

class ObjectOutputStream;
class ObjectInputStream;

struct Streamer
{
    virtual ~Streamer () {}
    virtual Shared<void> createObject (ObjectInputStream& in) = 0;
    virtual void writeObject (const Shared<void>& object, ObjectOutputStream& out) = 0;
};

Streamer* getStreamer (const utf8& javaName);
void registerStreamer (const utf8& javaName, Streamer* streamer);

inline const utf8 getJavaName (const utf8*)
{
    return "java.lang.String";
}

inline const utf8 getJavaName (const int8*)
{
    return "B";
}

inline const utf8 getJavaName (const int16*)
{
    return "S";
}

inline const utf8 getJavaName (const int32*)
{
    return "I";
}

inline const utf8 getJavaName (const int64*)
{
    return "J";
}

inline const utf8 getJavaName (const bool*)
{
    return "Z";
}

inline const utf8 getJavaName (const float*)
{
    return "F";
}

inline const utf8 getJavaName (const double*)
{
    return "D";
}

template <typename T>
inline const utf8 getJavaName (const Shared<T>*)
{
    return getJavaName((const T*)NULL);
}

template <typename T>
inline const utf8 getJavaName (const std::vector<T>*)
{
    return "[" + getJavaName((const T*)NULL);
}

template <typename T>
inline const utf8 getJavaName (const std::vector< Shared<T> >*)
{
    return "[L" + getJavaName((const T*)NULL) + ";";
}

template <typename T>
inline const utf8 getJavaName (const T*)
{
    return T::javaName();
}

template <>
inline const utf8 getJavaName<Streamable> (const Streamable*)
{
    return "java.lang.Object";
}

template <typename T>
inline const utf8 getObjectName (T* object)
{
    if (boost::is_base_of<Streamable, T>::value) {
        // Look up the object's class virtually
        return ((Streamable*)object)->getJavaClassName();
    } else {
        // Everything else is assumed to be a final class
        return getJavaName(object);
    }
}
