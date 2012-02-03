#include "Util.h"

void presents::internal::log(const char* format, ...)
{
	va_list argumentList;
	va_start(argumentList, format);
    
	char buffer[1024];
#ifdef PRESENTS_COMPILER_MSVC
	if (_vsnprintf(buffer, 1024, format, argumentList) > 0) {
#else
    if (vsprintf(buffer, format, argumentList) > 0) {
#endif
            presents::log(buffer);
    }

	va_end(argumentList);
}