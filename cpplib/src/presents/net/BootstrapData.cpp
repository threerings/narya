#include "presents/stable.h"
#include "BootstrapData.h"
#include "presents/Streamer.h"

using namespace presents::net;

DEFINE_STREAMABLE("com.threerings.presents.net.BootstrapData", BootstrapData);

void BootstrapData::readObject (ObjectInputStream& in)
{
    connectionId = int32(in.readInt());
    clientOid = int32(in.readInt());
    services = boost::static_pointer_cast< std::vector< Shared<presents::data::InvocationMarshaller> > >(in.readField(JAVA_LIST_NAME()));
}

void BootstrapData::writeObject (ObjectOutputStream& out) const
{
    out.writeInt(connectionId);
    out.writeInt(clientOid);
    out.writeField(services, JAVA_LIST_NAME());
}
