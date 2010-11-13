#include "stable.h"
#include "Streamer.h"

#include <map>
#include <string>
#include <boost/format.hpp>

#include "ObjectInputStream.h"
#include "ObjectOutputStream.h"
#include "PresentsError.h"

#include "streamers/StringStreamer.h"
#include "streamers/StreamableStreamer.h"
#include "streamers/VectorStreamer.h"

#include "Util.h"

class StreamerMap
{
private:
    typedef std::map<utf8, Streamer*> Map;

public:
    void insert (const utf8& javaName, Streamer* streamer)
    {
        _map[javaName] = streamer;
    }

    Streamer* get (const utf8& javaName)
    {    
        Map::iterator it = _map.find(javaName);
        if (it == _map.end()) {
            throw PresentsError((boost::format("Unable to find Streamer for %1%") % javaName).str());
        }
        return it->second;
    }

    ~StreamerMap ()
    {
        for (Map::iterator iter = _map.begin(); iter != _map.end(); ++iter) {
            delete iter->second;
        }
    }

private:
    Map _map;
};

namespace
{
    template <typename T>
    void registerPrimitiveArrayStreamer (StreamerMap& map)
    {
        map.insert(getJavaName((const std::vector<T>*)NULL), new VectorStreamer<T>());
    }

    template <typename T>
    void registerObjectArrayStreamer (StreamerMap& map)
    {
        map.insert(getJavaName<>((std::vector< Shared<T> >*)NULL), new VectorStreamer<T>());
    }
}

StreamerMap* getMap ()
{
    static bool gInited = false;
    static StreamerMap gMap;
    
    if (!gInited) {
        gInited = true;

        gMap.insert(getJavaName((utf8*)NULL), new StringStreamer());
        gMap.insert(JAVA_LIST_NAME(), new VectorStreamer< Streamable >());

        registerObjectArrayStreamer<Streamable>(gMap);
        registerObjectArrayStreamer<utf8>(gMap);

        registerPrimitiveArrayStreamer<int8>(gMap);
        registerPrimitiveArrayStreamer<int16>(gMap);
        registerPrimitiveArrayStreamer<int32>(gMap);
        registerPrimitiveArrayStreamer<int64>(gMap);
        registerPrimitiveArrayStreamer<float>(gMap);
        registerPrimitiveArrayStreamer<double>(gMap);
        registerPrimitiveArrayStreamer<bool>(gMap);
    }

    return &gMap;
}

void registerStreamer (const utf8& javaName, Streamer* streamer)
{
    getMap()->insert(javaName, streamer);
}

Streamer* getStreamer (const utf8& javaName)
{
    return getMap()->get(javaName);
}
