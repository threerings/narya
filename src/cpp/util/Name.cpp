#include "presents/stable.h"
#include "Name.h"

using namespace util;

DEFINE_STREAMABLE("com.threerings.util.Name", Name);

void Name::readObject (ObjectInputStream& in)
{
    name = in.readField< utf8 >();
}

void Name::writeObject (ObjectOutputStream& out) const
{
    out.writeField(name);
}
