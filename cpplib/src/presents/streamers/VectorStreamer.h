#pragma once

#include "../Streamer.h"
#include "presents/ArrayMask.h"

template <typename T>
class VectorStreamer : public Streamer
{
public:
    Shared<void> createObject (ObjectInputStream& in);
    void writeObject (const Shared<void>& object, ObjectOutputStream& out);
};

template <typename T>
Shared<void> VectorStreamer<T>::createObject (ObjectInputStream& in)
{
    int length = in.readInt();
    Shared< std::vector< Shared<T> > > v(new std::vector< Shared<T> >(length));
    for (int ii = 0; ii < length; ++ii) {
        Shared<void> read = in.readObject();
        (*v)[ii] = boost::static_pointer_cast<T>(read);
    }
    return v;
}

template <typename T>
void VectorStreamer<T>::writeObject (const Shared<void>& object, ObjectOutputStream& out)
{
    Shared< std::vector< Shared<T> > >& v = (Shared< std::vector< Shared<T> > >&)(object);
    int length = v->size();
    out.writeInt(length);
    for (int ii = 0; ii < length; ++ii) {
        out.writeObject((*v)[ii]);
    }
}

template <>
Shared<void> VectorStreamer<int8>::createObject (ObjectInputStream& in)
{
    int32 length = in.readInt();
    Shared< std::vector<int8> > v(new std::vector<int8>(length));
    for (int ii = 0; ii < length; ++ii) {
        (*v)[ii] = in.readByte();
    }
    return v;
}

template <>
void VectorStreamer<int8>::writeObject (const Shared<void>& object, ObjectOutputStream& out)
{
    const Shared< std::vector<int8> >& v = (Shared< std::vector<int8> >&)(object);
    int length = v->size();
    out.writeInt(length);
    for (int ii = 0; ii < length; ++ii) {
        out.writeByte((*v)[ii]);
    }
}

template <>
Shared<void> VectorStreamer<int16>::createObject (ObjectInputStream& in)
{
    int32 length = in.readInt();
    Shared< std::vector<int16> > v(new std::vector<int16>(length));
    for (int ii = 0; ii < length; ++ii) {
        (*v)[ii] = in.readShort();
    }
    return v;
}

template <>
void VectorStreamer<int16>::writeObject (const Shared<void>& object, ObjectOutputStream& out)
{
    const Shared< std::vector<int16> >& v = (Shared< std::vector<int16> >&)(object);
    int length = v->size();
    out.writeInt(length);
    for (int ii = 0; ii < length; ++ii) {
        out.writeShort((*v)[ii]);
    }
}

template <>
Shared<void> VectorStreamer<int32>::createObject (ObjectInputStream& in)
{
    int32 length = in.readInt();
    Shared< std::vector<int32> > v(new std::vector<int32>(length));
    for (int ii = 0; ii < length; ++ii) {
        (*v)[ii] = in.readInt();
    }
    return v;
}

template <>
void VectorStreamer<int32>::writeObject (const Shared<void>& object, ObjectOutputStream& out)
{
    const Shared< std::vector<int32> >& v = (Shared< std::vector<int32> >&)(object);
    int length = v->size();
    out.writeInt(length);
    for (int ii = 0; ii < length; ++ii) {
        out.writeInt((*v)[ii]);
    }
}

template <>
Shared<void> VectorStreamer<int64>::createObject (ObjectInputStream& in)
{
    int32 length = in.readInt();
    Shared< std::vector<int64> > v(new std::vector<int64>(length));
    for (int ii = 0; ii < length; ++ii) {
        (*v)[ii] = in.readLong();
    }
    return v;
}

template <>
void VectorStreamer<int64>::writeObject (const Shared<void>& object, ObjectOutputStream& out)
{
    const Shared< std::vector<int64> >& v = (Shared< std::vector<int64> >&)(object);
    int length = v->size();
    out.writeInt(length);
    for (int ii = 0; ii < length; ++ii) {
        out.writeLong((*v)[ii]);
    }
}

template <>
Shared<void> VectorStreamer<float>::createObject (ObjectInputStream& in)
{
    int32 length = in.readInt();
    Shared< std::vector<float> > v(new std::vector<float>(length));
    for (int ii = 0; ii < length; ++ii) {
        (*v)[ii] = in.readFloat();
    }
    return v;
}

template <>
void VectorStreamer<float>::writeObject (const Shared<void>& object, ObjectOutputStream& out)
{
    const Shared< std::vector<float> >& v = (Shared< std::vector<float> >&)(object);
    int length = v->size();
    out.writeInt(length);
    for (int ii = 0; ii < length; ++ii) {
        out.writeFloat((*v)[ii]);
    }
}

template <>
Shared<void> VectorStreamer<double>::createObject (ObjectInputStream& in)
{
    int32 length = in.readInt();
    Shared< std::vector<double> > v(new std::vector<double>(length));
    for (int ii = 0; ii < length; ++ii) {
        (*v)[ii] = in.readDouble();
    }
    return v;
}

template <>
void VectorStreamer<double>::writeObject (const Shared<void>& object, ObjectOutputStream& out)
{
    const Shared< std::vector<double> >& v = (Shared< std::vector<double> >&)(object);
    int length = v->size();
    out.writeInt(length);
    for (int ii = 0; ii < length; ++ii) {
        out.writeDouble((*v)[ii]);
    }
}

template <>
Shared<void> VectorStreamer<bool>::createObject (ObjectInputStream& in)
{
    int32 length = in.readInt();
    Shared< std::vector<bool> > v(new std::vector<bool>(length));
    for (int ii = 0; ii < length; ++ii) {
        (*v)[ii] = in.readBoolean();
    }
    return v;
}

template <>
void VectorStreamer<bool>::writeObject (const Shared<void>& object, ObjectOutputStream& out)
{
    const Shared< std::vector<bool> >& v = (Shared< std::vector<bool> >&)(object);
    int length = v->size();
    out.writeInt(length);
    for (int ii = 0; ii < length; ++ii) {
        out.writeBoolean((*v)[ii]);
    }
}

template <>
Shared<void> VectorStreamer< utf8 >::createObject (ObjectInputStream& in)
{
    int32 length = in.readInt();
    Shared< std::vector< Shared<utf8> > > v(new std::vector< Shared<utf8> >(length));
    ArrayMask mask(in);
    for (int ii = 0; ii < length; ++ii) {
        if (mask.isSet(ii)) {
            (*v)[ii] = Shared<utf8>(new utf8(in.readUTF()));
        } else {
            (*v)[ii] = Shared<utf8>();
        }
    }
    return v;
}

template <>
void VectorStreamer< utf8 >::writeObject (const Shared<void>& object, ObjectOutputStream& out)
{
    const Shared< std::vector< Shared<utf8> > >& v = (Shared< std::vector< Shared<utf8> > >&)(object);
    int length = v->size();
    out.writeInt(length);
    ArrayMask mask(length);
    for (int ii = 0; ii < length; ++ii) {
        if ((*v)[ii] != NULL) {
            mask.set(ii);
        }
    }
    mask.writeTo(out);
    for (int ii = 0; ii < length; ++ii) {
        out.writeUTF(*((*v)[ii]));
    }
}
