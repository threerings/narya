#include "presents/stable.h"
#include "StringStreamer.h"

#include <string>

#include "../ObjectInputStream.h"
#include "../ObjectOutputStream.h"

Shared<void> StringStreamer::createObject (ObjectInputStream& in)
{
    return Shared<void>(new utf8(in.readUTF()));
}

void StringStreamer::writeObject (const Shared<void>& object, ObjectOutputStream& out)
{
    out.writeUTF(*(boost::static_pointer_cast<utf8>(object)));
}
