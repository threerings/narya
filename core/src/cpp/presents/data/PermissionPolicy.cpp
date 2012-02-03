#include "presents/stable.h"
#include "PermissionPolicy.h"

using namespace presents::data;

DEFINE_STREAMABLE("com.threerings.presents.data.PermissionPolicy", PermissionPolicy);

void PermissionPolicy::readObject (ObjectInputStream& /*in*/)
{
}

void PermissionPolicy::writeObject (ObjectOutputStream& /*out*/) const
{
}
