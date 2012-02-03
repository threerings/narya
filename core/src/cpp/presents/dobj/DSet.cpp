#include "presents/stable.h"
#include "DSet.h"

using namespace presents::dobj;

DEFINE_STREAMABLE("com.threerings.presents.dobj.DSet", DSet);

void DSet::readObject (ObjectInputStream& in)
{
    size = in.readInt();
    entries = Shared<std::vector< Shared <Streamable> > >(new std::vector< Shared <Streamable> >(size));
    for (int ii = 0; ii < size; ii++) {
        (*entries)[ii] = boost::static_pointer_cast<Streamable>(in.readObject());
    }
}

void DSet::writeObject (ObjectOutputStream& out) const
{
    out.writeInt(size);
    out.writeField(entries);
}
