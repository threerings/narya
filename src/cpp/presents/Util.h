#pragma once

namespace presents {
	void log (const char* message);    

    namespace internal {
        void log (const char* format, ...);
    }
    
	template <class T>
	inline T reverseBytes (T source)
	{
		uint8* pData = reinterpret_cast<uint8*>(&source);
		std::reverse(pData, pData + sizeof(T));
		return source;
	}
    
	template <class T>
    T presentsToHost (T source)
    {
#ifdef PRESENTS_HOST_LITTLE_ENDIAN
		return reverseBytes(source);
#else
        return source;
#endif
	}
    
	template <class T>
    T hostToPresents (T source)
    {
#ifdef PRESENTS_HOST_LITTLE_ENDIAN
		return reverseBytes(source);
#else
        return source;
#endif
	}
}